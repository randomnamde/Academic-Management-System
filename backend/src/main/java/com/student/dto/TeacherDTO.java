package com.student.dto;

import com.student.entity.Teacher;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class TeacherDTO {

    private Long id;

    @NotBlank(message = "Teacher number is required")
    private String teacherNo;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Gender is required")
    private Teacher.Gender gender;

    private LocalDate birthday;

    private String phone;

    private String email;

    private Teacher.Title title;

    private String department;

    private LocalDate hireDate;

    private String password;
}
