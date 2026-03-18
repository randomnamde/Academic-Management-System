package com.student.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CollegeDTO {

    private String collegeCode;

    @NotBlank(message = "College name is required")
    private String collegeName;

    @NotBlank(message = "College english name is required")
    private String collegeNameEn;

    private String description;

    private Integer status;

    private String adminUsername;
}