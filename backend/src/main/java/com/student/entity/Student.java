package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("student")
public class Student {
    
    @TableId(value = "student_no", type = IdType.INPUT)
    private String studentNo;

    private Long id;
    
    private String userId;
    
    private String name;
    
    private Gender gender;
    
    private LocalDate birthday;
    
    private String idCard;
    
    private String phone;
    
    private String email;
    
    private String address;
    
    private String classId;
    
    @TableField(exist = false)
    private String className;

    @TableField(exist = false)
    private Long collegeId;

    @TableField(exist = false)
    private String collegeName;

    @TableField(exist = false)
    private String majorCode;

    @TableField(exist = false)
    private String majorName;
    
    private LocalDate enrollmentDate;
    
    private LocalDate graduationDate;
    
    private Status status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    public enum Gender {
        MALE, FEMALE
    }
    
    public enum Status {
        ENROLLED, GRADUATED, SUSPENDED, DROPPED
    }
}
