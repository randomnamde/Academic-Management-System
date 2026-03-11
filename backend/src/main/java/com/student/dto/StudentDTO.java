package com.student.dto;

import com.student.entity.Student;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class StudentDTO {

    private Long id;

    private String studentNo;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Gender is required")
    private Student.Gender gender;

    private LocalDate birthday;

    private String idCard;

    private String phone;

    private String email;

    private String address;

    private String classId;

    private LocalDate enrollmentDate;

    private String password;
}
