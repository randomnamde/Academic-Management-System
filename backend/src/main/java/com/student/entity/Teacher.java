package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("teacher")
public class Teacher {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String teacherNo;
    
    private String name;
    
    private Gender gender;
    
    private LocalDate birthday;
    
    private String phone;
    
    private String email;
    
    private Title title;
    
    private String department;

    private Long collegeId;
    
    private LocalDate hireDate;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    public enum Gender {
        MALE, FEMALE
    }
    
    public enum Title {
        LECTURER, ASSOCIATE_PROFESSOR, PROFESSOR
    }
}
