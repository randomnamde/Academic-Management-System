package com.student.dto;

import com.student.entity.Course;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseDTO {

    @NotBlank(message = "Course name is required")
    private String courseName;

    @NotBlank(message = "Course code is required")
    private String courseCode;

    @NotNull(message = "Credit is required")
    private BigDecimal credit;

    @NotNull(message = "Hours is required")
    private Integer hours;

    @NotNull(message = "Category is required")
    private Course.Category category;

    private String description;

    @NotNull(message = "Status is required")
    private Integer status;
}