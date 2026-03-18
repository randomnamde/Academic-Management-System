package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.UpdateUserRolesDTO;
import com.student.dto.BatchResetPasswordDTO;
import com.student.dto.PasswordVerificationDTO;
import com.student.dto.UpdatePasswordDTO;
import com.student.dto.UpdateProfileDTO;
import com.student.entity.Class;
import com.student.entity.College;
import com.student.entity.Student;
import com.student.entity.SysUser;
import com.student.entity.Teacher;
import com.student.mapper.ClassMapper;
import com.student.mapper.CollegeMapper;
import com.student.mapper.StudentMapper;
import com.student.mapper.TeacherMapper;
import com.student.security.CurrentUserService;
import com.student.security.PermissionService;
import com.student.security.RoleCode;
import com.student.service.SysUserService;
import com.student.vo.ResultVO;
import com.student.vo.RbacUserListItemVO;
import com.student.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final SysUserService sysUserService;
    private final PermissionService permissionService;
    private final CurrentUserService currentUserService;
    private final TeacherMapper teacherMapper;
    private final StudentMapper studentMapper;
    private final ClassMapper classMapper;
    private final CollegeMapper collegeMapper;

    @GetMapping("/info")
    public ResultVO<UserInfoVO> info(Authentication authentication) {
        if (authentication == null) {
            return ResultVO.error(401, "Unauthorized");
        }
        SysUser user = sysUserService.getByUsername(authentication.getName());
        if (user != null) {
            user.setPassword(null);
        }
        if (user == null) {
            return ResultVO.error(404, "User not found");
        }
        var roleCodes = sysUserService.getRoleCodes(user.getId());
        var orderedRoles = com.student.security.RoleCode.sortByPriority(roleCodes);
        UserInfoVO vo = new UserInfoVO();
        BeanUtils.copyProperties(user, vo);
        vo.setAccount(user.getUsername());
        String collegeCode = currentUserService.resolveCurrentCollegeCode(authentication);
        if (collegeCode != null) {
            vo.setCollegeCode(collegeCode);
            College college = collegeMapper.selectById(collegeCode);
            vo.setCollegeName(college != null ? college.getCollegeName() : null);
        }
        if (currentUserService.isTeacher(authentication)) {
            Teacher teacher = currentUserService.getCurrentTeacher(authentication);
            if (teacher != null) {
                vo.setTeacherNo(teacher.getTeacherNo());
                vo.setTeacherDepartment(teacher.getDepartment());
            }
        }
        if (currentUserService.isStudent(authentication)) {
            Student student = currentUserService.getCurrentStudent(authentication);
            if (student != null) {
                vo.setClassId(student.getClassId());
                vo.setStudentNo(student.getStudentNo());
                if (student.getClassId() != null) {
                    Class currentClass = classMapper.selectByClassCode(student.getClassId());
                    vo.setClassName(currentClass != null ? currentClass.getClassName() : null);
                }
            }
        }
        RoleCode primaryRoleCode = orderedRoles.isEmpty() ? null : orderedRoles.get(0);
        vo.setRole(RoleCode.toUserRole(primaryRoleCode));
        vo.setPrimaryRole(primaryRoleCode == null ? null : primaryRoleCode.name());
        vo.setRoles(orderedRoles.stream().map(RoleCode::name).collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new)));
        vo.setPermissions(new LinkedHashSet<>(permissionService.resolvePermissions(roleCodes)));
        return ResultVO.success(vo);
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Page<RbacUserListItemVO>> list(@RequestParam(defaultValue = "1") Integer page,
                                                   @RequestParam(defaultValue = "10") Integer size,
                                                   @RequestParam(required = false) String account,
                                                   @RequestParam(required = false) String username,
                                                   @RequestParam(required = false) String realName,
                                                   @RequestParam(required = false) SysUser.Role role,
                                                   @RequestParam(required = false) RoleCode roleCode,
                                                   @RequestParam(required = false) String collegeCode,
                                                   @RequestParam(required = false) String classId,
                                                   @RequestParam(required = false) Integer status,
                                                   Authentication authentication) {
        String scopedCollegeCode = currentUserService.resolveManagedCollegeCode(authentication);
        String effectiveCollegeCode = scopedCollegeCode != null ? scopedCollegeCode : collegeCode;
        RoleCode effectiveRoleCode = roleCode != null ? roleCode : RoleCode.fromUserRole(role);
        String accountKeyword = account != null && !account.isBlank() ? account : username;

        if (classId != null) {
            Class targetClass = classMapper.selectByClassCode(classId);
            if (targetClass == null) {
                return ResultVO.success(emptyUserPage(page, size));
            }
            if (scopedCollegeCode != null && !Objects.equals(scopedCollegeCode, targetClass.getCollegeCode())) {
                return ResultVO.error(403, "Forbidden");
            }
            if (effectiveCollegeCode != null && !Objects.equals(effectiveCollegeCode, targetClass.getCollegeCode())) {
                return ResultVO.success(emptyUserPage(page, size));
            }
        }

        List<SysUser> users = sysUserService.lambdaQuery()
                .like(accountKeyword != null && !accountKeyword.isBlank(), SysUser::getUsername, accountKeyword)
                .like(realName != null && !realName.isBlank(), SysUser::getRealName, realName)
                .eq(status != null, SysUser::getStatus, status)
                .orderByDesc(SysUser::getCreateTime)
                .list();

        Set<Long> scopedUserIds = scopedCollegeCode == null ? null : resolveScopedUserIds(scopedCollegeCode);
        Set<Long> userIds = users.stream()
                .map(SysUser::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, LinkedHashSet<String>> roleCodesMap = sysUserService.getRoleCodeNamesByUserIds(userIds);

        List<College> colleges = effectiveCollegeCode != null
                ? collegeMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<College>()
                .eq(College::getCollegeCode, effectiveCollegeCode))
                : collegeMapper.selectList(null);
        Map<String, College> collegeById = colleges.stream()
                .filter(item -> item.getCollegeCode() != null)
                .collect(Collectors.toMap(College::getCollegeCode, item -> item, (left, right) -> left));
        Map<String, College> collegeByAdminUserId = colleges.stream()
                .filter(item -> item.getAdminUserId() != null)
                .collect(Collectors.toMap(College::getAdminUserId, item -> item, (left, right) -> left));

        List<Class> classes = effectiveCollegeCode != null
                ? classMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Class>()
                .eq(Class::getCollegeCode, effectiveCollegeCode))
                : classMapper.selectList(null);
        Map<String, Class> classById = classes.stream()
                .filter(item -> item.getClassCode() != null)
                .collect(Collectors.toMap(Class::getClassCode, item -> item, (left, right) -> left));
        Map<String, List<Class>> classesByTeacherNo = classes.stream()
                .filter(item -> item.getTeacherNo() != null)
                .collect(Collectors.groupingBy(Class::getTeacherNo));

        Set<String> classIds = classById.keySet();
        List<Student> students = classIds.isEmpty() && effectiveCollegeCode != null
                ? List.of()
                : (classIds.isEmpty()
                ? studentMapper.selectList(null)
                : studentMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Student>()
                .in(Student::getClassId, classIds)));
        Map<String, Student> studentByUserId = students.stream()
                .filter(item -> item.getUserId() != null)
                .collect(Collectors.toMap(Student::getUserId, item -> item, (left, right) -> left));

        List<Teacher> teachers = effectiveCollegeCode != null
                ? teacherMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Teacher>()
                .eq(Teacher::getCollegeCode, effectiveCollegeCode))
                : teacherMapper.selectList(null);
        Map<String, Teacher> teacherByUserId = teachers.stream()
                .filter(item -> item.getUserId() != null)
                .collect(Collectors.toMap(Teacher::getUserId, item -> item, (left, right) -> left));

        List<RbacUserListItemVO> filtered = users.stream()
                .filter(item -> scopedUserIds == null || scopedUserIds.contains(item.getId()))
                .map(item -> toRbacUserListItem(
                        item,
                        roleCodesMap.getOrDefault(item.getId(), new LinkedHashSet<>()),
                        collegeByAdminUserId,
                        collegeById,
                        studentByUserId,
                        teacherByUserId,
                        classById,
                        classesByTeacherNo))
                .filter(item -> effectiveRoleCode == null || item.getRoles().contains(effectiveRoleCode.name()))
                .filter(item -> effectiveCollegeCode == null || Objects.equals(effectiveCollegeCode, item.getCollegeCode()))
                .filter(item -> classId == null || Objects.equals(classId, item.getClassId()) || matchesTeacherClass(classId, item.getUsername(), teacherByUserId, classesByTeacherNo))
                .toList();

        int from = Math.max((page - 1) * size, 0);
        int to = Math.min(from + size, filtered.size());
        List<RbacUserListItemVO> pageRecords = from >= filtered.size() ? List.of() : filtered.subList(from, to);
        Page<RbacUserListItemVO> result = new Page<>(page, size, filtered.size());
        result.setRecords(pageRecords);
        return ResultVO.success(result);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status, Authentication authentication) {
        String scopedCollegeCode = currentUserService.resolveManagedCollegeCode(authentication);
        if (scopedCollegeCode != null) {
            Set<Long> scopedUserIds = resolveScopedUserIds(scopedCollegeCode);
            if (!scopedUserIds.contains(id)) {
                return ResultVO.error(403, "Forbidden");
            }
        }
        sysUserService.updateStatus(id, status);
        return ResultVO.success();
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResultVO<Void> updateRoles(@PathVariable Long id,
                                      @RequestBody @Validated UpdateUserRolesDTO dto,
                                      Authentication authentication) {
        if (authentication == null) {
            return ResultVO.error(401, "Unauthorized");
        }
        SysUser currentUser = sysUserService.getByUsername(authentication.getName());
        if (currentUser != null && Objects.equals(currentUser.getId(), id)) {
            return ResultVO.error(400, "Cannot modify current user roles");
        }

        LinkedHashSet<RoleCode> roleCodes = dto.getRoleCodes().stream()
                .map(RoleCode::from)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (roleCodes.isEmpty()) {
            return ResultVO.error(400, "Role set cannot be empty");
        }

        sysUserService.assignRoles(id, roleCodes);
        return ResultVO.success();
    }

    @PutMapping("/password")
    public ResultVO<Void> updatePassword(@RequestBody @Validated UpdatePasswordDTO dto, Authentication authentication) {
        if (authentication == null) {
            return ResultVO.error(401, "Unauthorized");
        }
        SysUser currentUser = sysUserService.getByUsername(authentication.getName());
        if (currentUser == null) {
            return ResultVO.error(404, "User not found");
        }
        sysUserService.updatePassword(currentUser.getId(), dto.getOldPassword(), dto.getNewPassword());
        return ResultVO.success();
    }

    @PutMapping("/password/reset-all")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResultVO<Integer> resetAllPasswords(@RequestBody @Validated PasswordVerificationDTO dto,
                                               Authentication authentication) {
        if (authentication == null) {
            return ResultVO.error(401, "Unauthorized");
        }
        SysUser currentUser = sysUserService.getByUsername(authentication.getName());
        if (currentUser == null) {
            return ResultVO.error(404, "User not found");
        }
        sysUserService.verifyPassword(currentUser.getId(), dto.getOperatorPassword());
        return ResultVO.success(sysUserService.resetAllPasswordsToInitialPassword());
    }

    @PutMapping("/{id}/password/reset")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResultVO<Void> resetUserPassword(@PathVariable Long id,
                                            @RequestBody @Validated PasswordVerificationDTO dto,
                                            Authentication authentication) {
        if (authentication == null) {
            return ResultVO.error(401, "Unauthorized");
        }
        SysUser currentUser = sysUserService.getByUsername(authentication.getName());
        if (currentUser == null) {
            return ResultVO.error(404, "User not found");
        }
        sysUserService.verifyPassword(currentUser.getId(), dto.getOperatorPassword());
        sysUserService.resetPasswordToInitialPassword(id);
        return ResultVO.success();
    }

    @PutMapping("/password/reset-batch")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResultVO<Integer> resetBatchUserPasswords(@RequestBody @Validated BatchResetPasswordDTO dto,
                                                     Authentication authentication) {
        if (authentication == null) {
            return ResultVO.error(401, "Unauthorized");
        }
        SysUser currentUser = sysUserService.getByUsername(authentication.getName());
        if (currentUser == null) {
            return ResultVO.error(404, "User not found");
        }
        sysUserService.verifyPassword(currentUser.getId(), dto.getOperatorPassword());
        return ResultVO.success(sysUserService.resetPasswordsToInitialPassword(dto.getUserIds()));
    }

    @PutMapping("/profile")
    public ResultVO<Void> updateProfile(@RequestBody @Validated UpdateProfileDTO dto, Authentication authentication) {
        if (authentication == null) {
            return ResultVO.error(401, "Unauthorized");
        }
        SysUser currentUser = sysUserService.getByUsername(authentication.getName());
        if (currentUser == null) {
            return ResultVO.error(404, "User not found");
        }
        sysUserService.updateProfile(currentUser.getId(), dto);
        return ResultVO.success();
    }

    @PostMapping("/avatar")
    public ResultVO<String> uploadAvatar(@RequestParam("file") MultipartFile file, Authentication authentication) {
        if (authentication == null) {
            return ResultVO.error(401, "Unauthorized");
        }
        SysUser currentUser = sysUserService.getByUsername(authentication.getName());
        if (currentUser == null) {
            return ResultVO.error(404, "User not found");
        }
        String avatarUrl = sysUserService.uploadAvatar(currentUser.getId(), file);
        return ResultVO.success(avatarUrl);
    }

    private Set<Long> resolveScopedUserIds(String scopedCollegeCode) {
        Set<String> usernames = new HashSet<>();
        var college = collegeMapper.selectById(scopedCollegeCode);
        if (college != null && college.getAdminUserId() != null) {
            usernames.add(college.getAdminUserId());
        }
        List<Teacher> teachers = teacherMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Teacher>()
                        .eq(Teacher::getCollegeCode, scopedCollegeCode));
        for (Teacher teacher : teachers) {
            if (teacher.getUserId() != null) {
                usernames.add(teacher.getUserId());
            }
        }
        List<Class> classes = classMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Class>()
                        .eq(Class::getCollegeCode, scopedCollegeCode));
        Set<String> classIds = new HashSet<>();
        for (Class clazz : classes) {
            if (clazz.getClassCode() != null) {
                classIds.add(clazz.getClassCode());
            }
        }
        if (!classIds.isEmpty()) {
            List<Student> students = studentMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Student>()
                            .in(Student::getClassId, classIds));
            for (Student student : students) {
                if (student.getUserId() != null) {
                    usernames.add(student.getUserId());
                }
            }
        }
        if (usernames.isEmpty()) {
            return Set.of();
        }
        return sysUserService.lambdaQuery()
                .in(SysUser::getUsername, usernames)
                .list()
                .stream()
                .map(SysUser::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Page<RbacUserListItemVO> emptyUserPage(Integer page, Integer size) {
        Page<RbacUserListItemVO> result = new Page<>(page, size, 0);
        result.setRecords(List.of());
        return result;
    }

    private boolean matchesTeacherClass(String classId,
                                        String username,
                                        Map<String, Teacher> teacherByUserId,
                                        Map<String, List<Class>> classesByTeacherNo) {
        if (classId == null || username == null) {
            return false;
        }
        Teacher teacher = teacherByUserId.get(username);
        if (teacher == null || teacher.getTeacherNo() == null) {
            return false;
        }
        List<Class> teacherClasses = classesByTeacherNo.getOrDefault(teacher.getTeacherNo(), List.of());
        return teacherClasses.stream().anyMatch(item -> Objects.equals(item.getClassCode(), classId));
    }

    private RbacUserListItemVO toRbacUserListItem(SysUser user,
                                                  LinkedHashSet<String> storedRoleNames,
                                                  Map<String, College> collegeByAdminUserId,
                                                  Map<String, College> collegeById,
                                                  Map<String, Student> studentByUserId,
                                                  Map<String, Teacher> teacherByUserId,
                                                  Map<String, Class> classById,
                                                  Map<String, List<Class>> classesByTeacherNo) {
        LinkedHashSet<RoleCode> parsedRoles = storedRoleNames.stream()
                .map(RoleCode::from)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (parsedRoles.isEmpty() && user.getRole() != null) {
            RoleCode fallback = RoleCode.fromUserRole(user.getRole());
            if (fallback != null) {
                parsedRoles.add(fallback);
            }
        }

        LinkedHashSet<String> orderedRoleNames = RoleCode.sortByPriority(parsedRoles).stream()
                .map(RoleCode::name)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        RoleCode primaryRoleCode = RoleCode.selectPrimary(parsedRoles);
        SysUser.Role primaryRole = primaryRoleCode != null ? RoleCode.toUserRole(primaryRoleCode) : user.getRole();

        String collegeCode = null;
        String collegeName = null;
        String classId = null;
        String classDisplayName = null;

        College adminCollege = collegeByAdminUserId.get(user.getUsername());
        if (adminCollege != null) {
            collegeCode = adminCollege.getCollegeCode();
            collegeName = adminCollege.getCollegeName();
        }

        Student student = studentByUserId.get(user.getUsername());
        if (student != null && student.getClassId() != null) {
            Class clazz = classById.get(student.getClassId());
            classId = student.getClassId();
            classDisplayName = clazz != null ? clazz.getClassName() : null;
            if (collegeCode == null && clazz != null) {
                collegeCode = clazz.getCollegeCode();
                College college = collegeById.get(clazz.getCollegeCode());
                collegeName = college != null ? college.getCollegeName() : null;
            }
        }

        Teacher teacher = teacherByUserId.get(user.getUsername());
        if (teacher != null) {
            if (collegeCode == null && teacher.getCollegeCode() != null) {
                collegeCode = teacher.getCollegeCode();
                College college = collegeById.get(teacher.getCollegeCode());
                collegeName = college != null ? college.getCollegeName() : null;
            }

            List<Class> teacherClasses = classesByTeacherNo.getOrDefault(teacher.getTeacherNo(), List.of());
            if (!teacherClasses.isEmpty() && classId == null) {
                Class firstClass = teacherClasses.get(0);
                classId = firstClass.getClassCode();
                classDisplayName = teacherClasses.size() == 1
                        ? firstClass.getClassName()
                        : firstClass.getClassName() + " and " + teacherClasses.size() + " more";
            }
        }

        RbacUserListItemVO item = new RbacUserListItemVO();
        item.setId(user.getId());
        item.setAccount(user.getUsername());
        item.setUsername(user.getUsername());
        item.setRealName(user.getRealName());
        item.setRole(primaryRole);
        item.setPrimaryRole(primaryRoleCode == null ? (primaryRole == null ? null : primaryRole.name()) : primaryRoleCode.name());
        item.setRoles(orderedRoleNames);
        item.setPhone(user.getPhone());
        item.setEmail(user.getEmail());
        item.setStatus(user.getStatus());
        item.setCollegeCode(collegeCode);
        item.setCollegeName(collegeName);
        item.setClassId(classId);
        item.setClassDisplayName(classDisplayName);
        return item;
    }
}




