package com.student.security;

import com.student.entity.SysUser;

import java.util.Comparator;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public enum RoleCode {
    SCHOOL_ADMIN(100),
    COLLEGE_ADMIN(80),
    HOMEROOM_TEACHER(60),
    COURSE_TEACHER(40),
    STUDENT(20);

    private final int priority;

    RoleCode(int priority) {
        this.priority = priority;
    }

    public int priority() {
        return priority;
    }

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

    public static RoleCode fromUserRole(SysUser.Role role) {
        if (role == null) {
            return null;
        }
        return RoleCode.from(role.name());
    }

    public static SysUser.Role toUserRole(RoleCode code) {
        if (code == null) {
            return null;
        }
        return SysUser.Role.valueOf(code.name());
    }

    public static Set<String> toAuthorities(Set<RoleCode> roleCodes) {
        Set<String> authorities = new LinkedHashSet<>();
        for (RoleCode roleCode : roleCodes) {
            authorities.add(roleCode.name());
        }
        return authorities;
    }

    public static List<RoleCode> sortByPriority(Set<RoleCode> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return List.of();
        }
        return roleCodes.stream()
                .sorted(Comparator.comparingInt(RoleCode::priority).reversed())
                .collect(Collectors.toList());
    }

    public static RoleCode selectPrimary(Set<RoleCode> roleCodes) {
        return sortByPriority(roleCodes).stream().findFirst().orElse(null);
    }
}
