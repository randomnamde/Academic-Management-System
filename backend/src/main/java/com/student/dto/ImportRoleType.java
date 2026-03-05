package com.student.dto;

import com.student.security.RoleCode;

public enum ImportRoleType {
    COLLEGE_ADMIN('T', RoleCode.COLLEGE_ADMIN),
    HOMEROOM_TEACHER('T', RoleCode.HOMEROOM_TEACHER),
    COURSE_TEACHER('T', RoleCode.COURSE_TEACHER),
    STUDENT('S', RoleCode.STUDENT);

    private final char accountPrefix;
    private final RoleCode roleCode;

    ImportRoleType(char accountPrefix, RoleCode roleCode) {
        this.accountPrefix = accountPrefix;
        this.roleCode = roleCode;
    }

    public char accountPrefix() {
        return accountPrefix;
    }

    public RoleCode roleCode() {
        return roleCode;
    }
}
