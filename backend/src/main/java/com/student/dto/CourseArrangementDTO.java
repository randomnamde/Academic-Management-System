package com.student.dto;

import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class CourseArrangementDTO {

    private Long id;

    @NotNull(message = "Course id is required")
    private Long courseId;

    @NotNull(message = "Teacher id is required")
    private Long teacherId;

    @NotNull(message = "Class id is required")
    private Long classId;

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
