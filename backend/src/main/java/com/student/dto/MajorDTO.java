package com.student.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MajorDTO {

    @NotBlank(message = "Major name is required")
    private String majorName;

    private String majorAbbreviation;

    @NotNull(message = "College is required")
    private Long collegeId;

    private String description;

    private Integer status;
}
