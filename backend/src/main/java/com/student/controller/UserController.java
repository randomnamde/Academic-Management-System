package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.UpdateUserRolesDTO;
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
        Long collegeId = currentUserService.resolveCurrentCollegeId(authentication);
        if (collegeId != null) {
            vo.setCollegeId(collegeId);
            College college = collegeMapper.selectById(collegeId);
            vo.setCollegeName(college != null ? college.getCollegeName() : null);
        }
        if (currentUserService.isTeacher(authentication)) {
            Teacher teacher = currentUserService.getCurrentTeacher(authentication);
            if (teacher != null) {
                vo.setTeacherId(teacher.getId());
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
                    Class currentClass = classMapper.selectById(student.getClassId());
                    vo.setClassName(currentClass != null ? currentClass.getClassName() : null);
                }
            }
        }
        RoleCode primaryRoleCode = orderedRoles.isEmpty() ? null : orderedRoles.get(0);
        vo.setRole(RoleCode.toUserRole(primaryRoleCode));
        vo.setPrimaryRole(primaryRoleCode == null ? null : primaryRoleCode.name());
        vo.setRoles(orderedRoles.stream().map(RoleCode::name).collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new)));
        vo.setPermissions(permissionService.resolvePermissions(roleCodes));
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
                                                   @RequestParam(required = false) Long collegeId,
                                                   @RequestParam(required = false) Long classId,
                                                   @RequestParam(required = false) Integer status,
                                                   Authentication authentication) {
        Long scopedCollegeId = currentUserService.resolveManagedCollegeId(authentication);
        Long effectiveCollegeId = scopedCollegeId != null ? scopedCollegeId : collegeId;
        RoleCode effectiveRoleCode = roleCode != null ? roleCode : RoleCode.fromUserRole(role);
        String accountKeyword = account != null && !account.isBlank() ? account : username;

        if (classId != null) {
            Class targetClass = classMapper.selectById(classId);
            if (targetClass == null) {
                return ResultVO.success(emptyUserPage(page, size));
            }
            if (scopedCollegeId != null && !Objects.equals(scopedCollegeId, targetClass.getCollegeId())) {
                return ResultVO.error(403, "Forbidden");
            }
            if (effectiveCollegeId != null && !Objects.equals(effectiveCollegeId, targetClass.getCollegeId())) {
                return ResultVO.success(emptyUserPage(page, size));
            }
        }

        List<SysUser> users = sysUserService.lambdaQuery()
                .like(accountKeyword != null && !accountKeyword.isBlank(), SysUser::getUsername, accountKeyword)
                .like(realName != null && !realName.isBlank(), SysUser::getRealName, realName)
                .eq(status != null, SysUser::getStatus, status)
                .orderByDesc(SysUser::getCreateTime)
                .list();

        Set<Long> scopedUserIds = scopedCollegeId == null ? null : resolveScopedUserIds(scopedCollegeId);
        Set<Long> userIds = users.stream()
                .map(SysUser::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, LinkedHashSet<String>> roleCodesMap = sysUserService.getRoleCodeNamesByUserIds(userIds);

        List<College> colleges = effectiveCollegeId != null
                ? collegeMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<College>()
                .eq(College::getId, effectiveCollegeId))
                : collegeMapper.selectList(null);
        Map<Long, College> collegeById = colleges.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(College::getId, item -> item, (left, right) -> left));
        Map<Long, College> collegeByAdminUserId = colleges.stream()
                .filter(item -> item.getAdminUserId() != null)
                .collect(Collectors.toMap(College::getAdminUserId, item -> item, (left, right) -> left));

        List<Class> classes = effectiveCollegeId != null
                ? classMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Class>()
                .eq(Class::getCollegeId, effectiveCollegeId))
                : classMapper.selectList(null);
        Map<Long, Class> classById = classes.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(Class::getId, item -> item, (left, right) -> left));
        Map<Long, List<Class>> classesByTeacherId = classes.stream()
                .filter(item -> item.getTeacherId() != null)
                .collect(Collectors.groupingBy(Class::getTeacherId));

        Set<Long> classIds = classById.keySet();
        List<Student> students = classIds.isEmpty() && effectiveCollegeId != null
                ? List.of()
                : (classIds.isEmpty()
                ? studentMapper.selectList(null)
                : studentMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Student>()
                .in(Student::getClassId, classIds)));
        Map<Long, Student> studentByUserId = students.stream()
                .filter(item -> item.getUserId() != null)
                .collect(Collectors.toMap(Student::getUserId, item -> item, (left, right) -> left));

        List<Teacher> teachers = effectiveCollegeId != null
                ? teacherMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Teacher>()
                .eq(Teacher::getCollegeId, effectiveCollegeId))
                : teacherMapper.selectList(null);
        Map<Long, Teacher> teacherByUserId = teachers.stream()
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
                        classesByTeacherId))
                .filter(item -> effectiveRoleCode == null || item.getRoles().contains(effectiveRoleCode.name()))
                .filter(item -> effectiveCollegeId == null || Objects.equals(effectiveCollegeId, item.getCollegeId()))
                .filter(item -> classId == null || Objects.equals(classId, item.getClassId()) || matchesTeacherClass(classId, item.getId(), teacherByUserId, classesByTeacherId))
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
        Long scopedCollegeId = currentUserService.resolveManagedCollegeId(authentication);
        if (scopedCollegeId != null) {
            Set<Long> scopedUserIds = resolveScopedUserIds(scopedCollegeId);
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

    private Set<Long> resolveScopedUserIds(Long scopedCollegeId) {
        Set<Long> userIds = new HashSet<>();
        var college = collegeMapper.selectById(scopedCollegeId);
        if (college != null && college.getAdminUserId() != null) {
            userIds.add(college.getAdminUserId());
        }
        List<Teacher> teachers = teacherMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Teacher>()
                        .eq(Teacher::getCollegeId, scopedCollegeId));
        for (Teacher teacher : teachers) {
            if (teacher.getUserId() != null) {
                userIds.add(teacher.getUserId());
            }
        }
        List<Class> classes = classMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Class>()
                        .eq(Class::getCollegeId, scopedCollegeId));
        Set<Long> classIds = new HashSet<>();
        for (Class clazz : classes) {
            if (clazz.getId() != null) {
                classIds.add(clazz.getId());
            }
        }
        if (!classIds.isEmpty()) {
            List<Student> students = studentMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Student>()
                            .in(Student::getClassId, classIds));
            for (Student student : students) {
                if (student.getUserId() != null) {
                    userIds.add(student.getUserId());
                }
            }
        }
        return userIds;
    }

    private Page<RbacUserListItemVO> emptyUserPage(Integer page, Integer size) {
        Page<RbacUserListItemVO> result = new Page<>(page, size, 0);
        result.setRecords(List.of());
        return result;
    }

    private boolean matchesTeacherClass(Long classId,
                                        Long userId,
                                        Map<Long, Teacher> teacherByUserId,
                                        Map<Long, List<Class>> classesByTeacherId) {
        if (classId == null || userId == null) {
            return false;
        }
        Teacher teacher = teacherByUserId.get(userId);
        if (teacher == null || teacher.getId() == null) {
            return false;
        }
        List<Class> teacherClasses = classesByTeacherId.getOrDefault(teacher.getId(), List.of());
        return teacherClasses.stream().anyMatch(item -> Objects.equals(item.getId(), classId));
    }

    private RbacUserListItemVO toRbacUserListItem(SysUser user,
                                                  LinkedHashSet<String> storedRoleNames,
                                                  Map<Long, College> collegeByAdminUserId,
                                                  Map<Long, College> collegeById,
                                                  Map<Long, Student> studentByUserId,
                                                  Map<Long, Teacher> teacherByUserId,
                                                  Map<Long, Class> classById,
                                                  Map<Long, List<Class>> classesByTeacherId) {
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

        Long collegeId = null;
        String collegeName = null;
        Long classId = null;
        String classDisplayName = null;

        College adminCollege = collegeByAdminUserId.get(user.getId());
        if (adminCollege != null) {
            collegeId = adminCollege.getId();
            collegeName = adminCollege.getCollegeName();
        }

        Student student = studentByUserId.get(user.getId());
        if (student != null && student.getClassId() != null) {
            Class clazz = classById.get(student.getClassId());
            classId = student.getClassId();
            classDisplayName = clazz != null ? clazz.getClassName() : null;
            if (collegeId == null && clazz != null) {
                collegeId = clazz.getCollegeId();
                College college = collegeById.get(clazz.getCollegeId());
                collegeName = college != null ? college.getCollegeName() : null;
            }
        }

        Teacher teacher = teacherByUserId.get(user.getId());
        if (teacher != null) {
            if (collegeId == null && teacher.getCollegeId() != null) {
                collegeId = teacher.getCollegeId();
                College college = collegeById.get(teacher.getCollegeId());
                collegeName = college != null ? college.getCollegeName() : null;
            }

            List<Class> teacherClasses = classesByTeacherId.getOrDefault(teacher.getId(), List.of());
            if (!teacherClasses.isEmpty() && classId == null) {
                Class firstClass = teacherClasses.get(0);
                classId = firstClass.getId();
                classDisplayName = teacherClasses.size() == 1
                        ? firstClass.getClassName()
                        : firstClass.getClassName() + " 等" + teacherClasses.size() + "个班级";
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
        item.setCollegeId(collegeId);
        item.setCollegeName(collegeName);
        item.setClassId(classId);
        item.setClassDisplayName(classDisplayName);
        return item;
    }
}


