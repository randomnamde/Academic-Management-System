package com.student.security;

import com.student.entity.Class;
import com.student.entity.College;
import com.student.entity.Student;
import com.student.entity.SysUser;
import com.student.entity.Teacher;
import com.student.exception.BusinessException;
import com.student.mapper.ClassMapper;
import com.student.mapper.CollegeMapper;
import com.student.mapper.SysUserRoleMapper;
import com.student.mapper.TeacherMapper;
import com.student.service.StudentService;
import com.student.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CurrentUserService {

    private final SysUserService sysUserService;
    private final StudentService studentService;
    private final TeacherMapper teacherMapper;
    private final ClassMapper classMapper;
    private final CollegeMapper collegeMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

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

    public Set<RoleCode> getCurrentRoleCodes(Authentication authentication) {
        SysUser user = getCurrentUser(authentication);
        List<String> roleCodes = sysUserRoleMapper.selectRoleCodesByUserId(user.getId());
        Set<RoleCode> result = new LinkedHashSet<>();
        for (String value : roleCodes) {
            RoleCode code = RoleCode.from(value);
            if (code != null) {
                result.add(code);
            }
        }
        if (result.isEmpty()) {
            RoleCode fallback = RoleCode.fromUserRole(user.getRole());
            if (fallback != null) {
                result.add(fallback);
            }
        }
        return result;
    }

    public RoleCode getPrimaryRoleCode(Authentication authentication) {
        return RoleCode.selectPrimary(getCurrentRoleCodes(authentication));
    }

    public boolean hasRole(Authentication authentication, RoleCode roleCode) {
        return getCurrentRoleCodes(authentication).contains(roleCode);
    }

    public boolean hasAnyRole(Authentication authentication, RoleCode... roleCodes) {
        Set<RoleCode> current = getCurrentRoleCodes(authentication);
        for (RoleCode roleCode : roleCodes) {
            if (current.contains(roleCode)) {
                return true;
            }
        }
        return false;
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

    public Teacher getCurrentTeacher(Authentication authentication) {
        SysUser user = getCurrentUser(authentication);
        Teacher teacher = teacherMapper.selectByUserId(user.getId());
        if (teacher == null) {
            throw new BusinessException(404, "Teacher profile not found");
        }
        return teacher;
    }

    public boolean isStudent(Authentication authentication) {
        return getPrimaryRoleCode(authentication) == RoleCode.STUDENT;
    }

    public boolean isTeacher(Authentication authentication) {
        RoleCode primaryRole = getPrimaryRoleCode(authentication);
        return primaryRole == RoleCode.HOMEROOM_TEACHER || primaryRole == RoleCode.COURSE_TEACHER;
    }

    public boolean isAdmin(Authentication authentication) {
        return getPrimaryRoleCode(authentication) == RoleCode.SCHOOL_ADMIN;
    }

    public boolean isCollegeAdmin(Authentication authentication) {
        return getPrimaryRoleCode(authentication) == RoleCode.COLLEGE_ADMIN;
    }

    public Long resolveManagedCollegeId(Authentication authentication) {
        if (!isCollegeAdmin(authentication)) {
            return null;
        }
        SysUser user = getCurrentUser(authentication);
        College college = collegeMapper.selectByAdminUserId(user.getId());
        if (college == null) {
            throw new BusinessException(403, "Current account is not bound to a college");
        }
        return college.getId();
    }

    public Long resolveCurrentCollegeId(Authentication authentication) {
        if (isStudent(authentication)) {
            Student student = getCurrentStudent(authentication);
            if (student.getClassId() == null) {
                return null;
            }
            Class clazz = classMapper.selectById(student.getClassId());
            return clazz == null ? null : clazz.getCollegeId();
        }
        if (isTeacher(authentication)) {
            Teacher teacher = getCurrentTeacher(authentication);
            return teacher.getCollegeId();
        }
        return resolveManagedCollegeId(authentication);
    }
}
