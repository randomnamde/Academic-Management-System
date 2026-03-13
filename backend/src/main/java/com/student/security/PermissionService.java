package com.student.security;

import com.student.entity.SysUser;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class PermissionService {
    private static final Map<RoleCode, List<String>> PERMISSIONS_BY_ROLE = buildPermissionsByRole();

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
        RoleCode roleCode = RoleCode.fromUserRole(role);
        if (roleCode == null) {
            return List.of();
        }
        return resolvePermissions(Set.of(roleCode));
    }

    private List<String> resolvePermissions(RoleCode role) {
        return PERMISSIONS_BY_ROLE.getOrDefault(role, List.of());
    }

    private static Map<RoleCode, List<String>> buildPermissionsByRole() {
        EnumMap<RoleCode, List<String>> permissions = new EnumMap<>(RoleCode.class);

        permissions.put(RoleCode.SCHOOL_ADMIN, List.of("*:*", "*"));
        permissions.put(RoleCode.COLLEGE_ADMIN, List.of(
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
                "route:RBACUsers:view",
                "route:RBACUserList:view",
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
        ));
        permissions.put(RoleCode.HOMEROOM_TEACHER, List.of(
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
        ));
        permissions.put(RoleCode.COURSE_TEACHER, List.of(
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
        ));
        permissions.put(RoleCode.STUDENT, List.of(
                "route:Dashboard:view",
                "route:Analytics:view",
                "route:Score:view",
                "route:Attendance:view",
                "route:LeaveRequest:view",
                "route:Announcement:view",
                "route:Profile:view"
        ));

        return Map.copyOf(permissions);
    }
}
