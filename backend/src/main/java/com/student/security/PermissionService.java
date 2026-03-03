package com.student.security;

import com.student.entity.SysUser;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class PermissionService {

    public List<String> resolvePermissions(Set<RoleCode> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }
        Set<String> permissions = new LinkedHashSet<>();
        for (RoleCode role : roles) {
            permissions.addAll(resolvePermissions(role));
        }
        return List.copyOf(permissions);
    }

    public List<String> resolvePermissions(SysUser.Role role) {
        RoleCode roleCode = RoleCode.fromLegacy(role);
        if (roleCode == null) {
            return List.of();
        }
        return resolvePermissions(Set.of(roleCode));
    }

    private List<String> resolvePermissions(RoleCode role) {
        return switch (role) {
            case SCHOOL_ADMIN -> List.of("*:*", "*");
            case COLLEGE_ADMIN -> List.of(
                    "route:Dashboard:view",
                    "route:Analytics:view",
                    "route:Student:view",
                    "route:Teacher:view",
                    "route:Class:view",
                    "route:Course:view",
                    "route:CourseArrangement:view",
                    "route:Score:view",
                    "route:Attendance:view",
                    "route:LeaveRequest:view",
                    "route:Announcement:view",
                    "route:Profile:view",
                    "route:College:view",
                    "score:create",
                    "score:update",
                    "score:delete",
                    "attendance:create",
                    "attendance:update",
                    "attendance:delete",
                    "announcement:create",
                    "announcement:update",
                    "announcement:delete",
                    "announcement:publish",
                    "leave:approve"
            );
            case HOMEROOM_TEACHER -> List.of(
                    "route:Dashboard:view",
                    "route:Analytics:view",
                    "route:Student:view",
                    "route:Class:view",
                    "route:Course:view",
                    "route:CourseArrangement:view",
                    "route:Score:view",
                    "route:Attendance:view",
                    "route:LeaveRequest:view",
                    "route:Announcement:view",
                    "route:Profile:view",
                    "score:create",
                    "score:update",
                    "score:delete",
                    "attendance:create",
                    "attendance:update",
                    "attendance:delete",
                    "announcement:create",
                    "announcement:update",
                    "announcement:delete",
                    "announcement:publish",
                    "leave:approve"
            );
            case COURSE_TEACHER -> List.of(
                    "route:Dashboard:view",
                    "route:Analytics:view",
                    "route:Student:view",
                    "route:Class:view",
                    "route:Course:view",
                    "route:CourseArrangement:view",
                    "route:Score:view",
                    "route:Attendance:view",
                    "route:LeaveRequest:view",
                    "route:Announcement:view",
                    "route:Profile:view",
                    "score:create",
                    "score:update",
                    "score:delete",
                    "attendance:create",
                    "attendance:update",
                    "attendance:delete",
                    "announcement:create",
                    "announcement:update",
                    "announcement:delete",
                    "announcement:publish"
            );
            case STUDENT -> List.of(
                    "route:Dashboard:view",
                    "route:Analytics:view",
                    "route:Score:view",
                    "route:Attendance:view",
                    "route:LeaveRequest:view",
                    "route:Announcement:view",
                    "route:Profile:view"
            );
        };
    }
}
