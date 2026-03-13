package com.student.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CollegeDTO {

    private Long id;

    private String collegeCode;

    @NotBlank(message = "请输入学院名称")
    private String collegeName;

    @NotBlank(message = "请输入学院英文名称")
    private String collegeNameEn;

    private String description;

    private Integer status;

    private String adminUsername;
}
