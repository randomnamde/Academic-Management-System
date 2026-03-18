package com.student.dto;

import com.student.entity.Teacher;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TeacherDTO {

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

    private String collegeCode;

    private LocalDate hireDate;

    private String password;
}