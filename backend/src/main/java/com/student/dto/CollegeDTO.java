package com.student.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CollegeDTO {

    private Long id;

    @NotBlank(message = "请输入学院编码")
    private String collegeCode;

    @NotBlank(message = "请输入学院名称")
    private String collegeName;

    private String description;

    private Integer status;

    private String adminUsername;
}
