package com.student.security;

import com.student.entity.SysUser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PermissionService {

    public List<String> resolvePermissions(SysUser.Role role) {
        if (role == null) {
            return List.of();
        }
        return switch (role) {
            case ADMIN -> List.of("*:*", "*");
            case TEACHER -> List.of(
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
