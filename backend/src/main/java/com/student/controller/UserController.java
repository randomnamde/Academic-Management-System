package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.UpdatePasswordDTO;
import com.student.dto.UpdateProfileDTO;
import com.student.entity.Class;
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
import com.student.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        UserInfoVO vo = new UserInfoVO();
        BeanUtils.copyProperties(user, vo);
        vo.setPrimaryRole(roleCodes.isEmpty() ? null : roleCodes.iterator().next().name());
        vo.setRoles(roleCodes.stream().map(RoleCode::name).collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new)));
        vo.setPermissions(permissionService.resolvePermissions(roleCodes));
        return ResultVO.success(vo);
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultVO<Page<SysUser>> list(@RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer size,
                                        @RequestParam(required = false) String username,
                                        @RequestParam(required = false) String realName,
                                        @RequestParam(required = false) SysUser.Role role,
                                        @RequestParam(required = false) Integer status,
                                        Authentication authentication) {
        Long scopedCollegeId = currentUserService.resolveManagedCollegeId(authentication);
        if (scopedCollegeId != null) {
            Set<Long> scopedUserIds = resolveScopedUserIds(scopedCollegeId);
            List<SysUser> all = sysUserService.lambdaQuery()
                    .orderByDesc(SysUser::getCreateTime)
                    .list();
            List<SysUser> filtered = all.stream()
                    .filter(item -> scopedUserIds.contains(item.getId()))
                    .filter(item -> username == null || username.isBlank() || item.getUsername().contains(username))
                    .filter(item -> realName == null || realName.isBlank() || (item.getRealName() != null && item.getRealName().contains(realName)))
                    .filter(item -> role == null || role == item.getRole())
                    .filter(item -> status == null || status.equals(item.getStatus()))
                    .toList();
            int from = Math.max((page - 1) * size, 0);
            int to = Math.min(from + size, filtered.size());
            List<SysUser> pageRecords = from >= filtered.size() ? List.of() : filtered.subList(from, to);
            Page<SysUser> result = new Page<>(page, size, filtered.size());
            result.setRecords(pageRecords);
            return ResultVO.success(result);
        }

        Page<SysUser> pageParam = new Page<>(page, size);
        Page<SysUser> result = sysUserService.lambdaQuery()
                .like(username != null && !username.isBlank(), SysUser::getUsername, username)
                .like(realName != null && !realName.isBlank(), SysUser::getRealName, realName)
                .eq(role != null, SysUser::getRole, role)
                .eq(status != null, SysUser::getStatus, status)
                .orderByDesc(SysUser::getCreateTime)
                .page(pageParam);
        return ResultVO.success(result);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
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
}
