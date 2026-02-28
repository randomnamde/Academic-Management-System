package com.student.security;

import com.student.entity.Student;
import com.student.entity.SysUser;
import com.student.entity.Teacher;
import com.student.exception.BusinessException;
import com.student.mapper.TeacherMapper;
import com.student.service.StudentService;
import com.student.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserService {

    private final SysUserService sysUserService;
    private final StudentService studentService;
    private final TeacherMapper teacherMapper;

    public SysUser getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new BusinessException(401, "Unauthorized");
        }
        SysUser user = sysUserService.getByUsername(authentication.getName());
        if (user == null) {
            throw new BusinessException(401, "User not found");
        }
        return user;
    }

    public Long getCurrentStudentId(Authentication authentication) {
        SysUser user = getCurrentUser(authentication);
        Student student = studentService.getStudentByUserId(user.getId());
        if (student == null) {
            throw new BusinessException(404, "Student profile not found");
        }
        return student.getId();
    }

    public Student getCurrentStudent(Authentication authentication) {
        SysUser user = getCurrentUser(authentication);
        Student student = studentService.getStudentByUserId(user.getId());
        if (student == null) {
            throw new BusinessException(404, "Student profile not found");
        }
        return student;
    }

    public Long getCurrentTeacherId(Authentication authentication) {
        SysUser user = getCurrentUser(authentication);
        Teacher teacher = teacherMapper.selectByUserId(user.getId());
        if (teacher == null) {
            throw new BusinessException(404, "Teacher profile not found");
        }
        return teacher.getId();
    }

    public boolean isStudent(Authentication authentication) {
        return getCurrentUser(authentication).getRole() == SysUser.Role.STUDENT;
    }

    public boolean isTeacher(Authentication authentication) {
        return getCurrentUser(authentication).getRole() == SysUser.Role.TEACHER;
    }

    public boolean isAdmin(Authentication authentication) {
        return getCurrentUser(authentication).getRole() == SysUser.Role.ADMIN;
    }
}
