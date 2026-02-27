package com.student.dto;

import com.student.entity.Student;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class StudentDTO {

    private Long id;

    @NotBlank(message = "Student number is required")
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

    private Long classId;

    private LocalDate enrollmentDate;

    private String password;
}
