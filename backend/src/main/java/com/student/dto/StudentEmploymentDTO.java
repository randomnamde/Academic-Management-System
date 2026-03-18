package com.student.dto;

import com.student.entity.StudentEmployment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class StudentEmploymentDTO {

    private Long id;

    @NotBlank(message = "学号不能为空")
    private String studentId;

    @NotNull(message = "毕业年份不能为空")
    private Integer graduationYear;

    @NotNull(message = "就业状态不能为空")
    private StudentEmployment.EmploymentStatus employmentStatus;

    private String companyName;

    private StudentEmployment.CompanyType companyType;

    private String industry;

    private String position;

    private String salaryRange;

    private String workCity;

    private String workProvince;

    private StudentEmployment.ContractType contractType;

    private Integer socialInsurance;

    private String contactName;

    private String contactPhone;

    private String contactEmail;

    private String employmentProveUrl;

    private String graduationSchool;

    private String graduationMajor;

    private StudentEmployment.DegreeType degreeType;

    private String remarks;
}
