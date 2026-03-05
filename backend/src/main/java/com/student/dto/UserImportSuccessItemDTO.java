package com.student.dto;

import lombok.Data;

@Data
public class UserImportSuccessItemDTO {
    private Integer rowNumber;
    private ImportRoleType roleType;
    private String username;
    private String realName;
}
