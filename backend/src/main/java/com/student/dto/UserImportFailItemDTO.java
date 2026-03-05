package com.student.dto;

import lombok.Data;

import java.util.Map;

@Data
public class UserImportFailItemDTO {
    private Integer rowNumber;
    private ImportRoleType roleType;
    private String message;
    private Map<String, String> rowData;
}
