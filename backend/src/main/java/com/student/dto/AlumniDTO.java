package com.student.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AlumniDTO {

    private Long id;

    @NotBlank(message = "学号不能为空")
    private String studentId;

    @NotBlank(message = "姓名不能为空")
    private String name;

    private Integer gender;

    private String classCode;

    @NotNull(message = "毕业年份不能为空")
    private Integer graduationYear;

    private String majorCode;

    private String collegeCode;

    private String phone;

    private String email;

    private String wechat;

    private String currentCompany;

    private String currentPosition;

    private Integer workYears;

    private Integer alumniAssociationMember;

    private Integer volunteer;

    private Integer donation;

    private Integer status;
}
