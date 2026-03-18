package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("student_employment")
public class StudentEmployment {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("student_id")
    private String studentId;

    @TableField("graduation_year")
    private Integer graduationYear;

    @TableField("employment_status")
    private EmploymentStatus employmentStatus;

    @TableField("company_name")
    private String companyName;

    @TableField("company_type")
    private CompanyType companyType;

    @TableField("industry")
    private String industry;

    @TableField("position")
    private String position;

    @TableField("salary_range")
    private String salaryRange;

    @TableField("work_city")
    private String workCity;

    @TableField("work_province")
    private String workProvince;

    @TableField("contract_type")
    private ContractType contractType;

    @TableField("social_insurance")
    private Integer socialInsurance;

    @TableField("contact_name")
    private String contactName;

    @TableField("contact_phone")
    private String contactPhone;

    @TableField("contact_email")
    private String contactEmail;

    @TableField("employment_prove_url")
    private String employmentProveUrl;

    @TableField("graduation_school")
    private String graduationSchool;

    @TableField("graduation_major")
    private String graduationMajor;

    @TableField("degree_type")
    private DegreeType degreeType;

    @TableField("remarks")
    private String remarks;

    @TableField("recorder_no")
    private String recorderNo;

    @TableField(value = "record_time", fill = FieldFill.INSERT)
    private LocalDateTime recordTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 关联字段（不映射数据库）
    @TableField(exist = false)
    private String studentName;

    @TableField(exist = false)
    private String className;

    @TableField(exist = false)
    private String collegeName;

    @TableField(exist = false)
    private String majorName;

    public enum EmploymentStatus {
        EMPLOYED,      // 已就业
        GRADUATE_SCHOOL, // 升学
        SELF_EMPLOYED, // 自主创业
        UNEMPLOYED,    // 待业
        OTHER          // 其他
    }

    public enum CompanyType {
        GOVERNMENT,       // 政府机关
        STATE_OWNED,      // 国有企业
        PRIVATE,          // 民营企业
        FOREIGN,          // 外资企业
        JOINT_VENTURE,    // 合资企业
        OTHER             // 其他
    }

    public enum ContractType {
        FULL_TIME,      // 全职
        PART_TIME,      // 兼职
        INTERNSHIP,     // 实习
        LABOR_CONTRACT, // 劳务合同
        OTHER           // 其他
    }

    public enum DegreeType {
        MASTER,    // 硕士
        DOCTOR,   // 博士
        OTHER     // 其他
    }
}
