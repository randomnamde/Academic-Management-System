package com.student.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseArrangementDTO {

    private Long id;

    @NotNull(message = "College is required")
    private String collegeCode;

    @NotNull(message = "Course is required")
    private String courseCode;

    @NotNull(message = "Teacher is required")
    private String teacherNo;

    @NotNull(message = "Class is required")
    private String classId;

    @NotBlank(message = "Semester is required")
    private String semester;

    @NotBlank(message = "Schedule is required")
    private String schedule;

    private String room;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be greater than 0")
    private Integer capacity;

    private Integer status;
}