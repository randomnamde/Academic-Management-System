package com.student.security;

import com.student.entity.SysUser;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public enum RoleCode {
    SCHOOL_ADMIN,
    COLLEGE_ADMIN,
    HOMEROOM_TEACHER,
    COURSE_TEACHER,
    STUDENT;

    public static RoleCode from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return RoleCode.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public static RoleCode fromLegacy(SysUser.Role role) {
        if (role == null) {
            return null;
        }
        return switch (role) {
            case ADMIN -> SCHOOL_ADMIN;
            case TEACHER -> COURSE_TEACHER;
            case STUDENT -> STUDENT;
        };
    }

    public static SysUser.Role toLegacy(RoleCode code) {
        if (code == null) {
            return null;
        }
        return switch (code) {
            case SCHOOL_ADMIN, COLLEGE_ADMIN -> SysUser.Role.ADMIN;
            case HOMEROOM_TEACHER, COURSE_TEACHER -> SysUser.Role.TEACHER;
            case STUDENT -> SysUser.Role.STUDENT;
        };
    }

    public static Set<String> toAuthorities(Set<RoleCode> roleCodes) {
        Set<String> authorities = new LinkedHashSet<>();
        for (RoleCode roleCode : roleCodes) {
            authorities.add(roleCode.name());
            SysUser.Role legacy = toLegacy(roleCode);
            if (legacy != null) {
                authorities.add(legacy.name());
            }
        }
        return authorities;
    }
}

