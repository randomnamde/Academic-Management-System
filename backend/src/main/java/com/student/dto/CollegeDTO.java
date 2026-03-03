package com.student.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CollegeDTO {

    private Long id;

    @NotBlank(message = "College code is required")
    private String collegeCode;

    @NotBlank(message = "College name is required")
    private String collegeName;

    private String description;

    private Integer status;

    private Long adminUserId;
}

