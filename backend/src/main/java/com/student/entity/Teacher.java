package com.student.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("teacher")
public class Teacher {

    @TableId(value = "teacher_no", type = IdType.INPUT)
    private String teacherNo;

    @TableField("id")
    private Long id;

    private String userId;

    private String name;

    private Gender gender;

    private LocalDate birthday;

    private String phone;

    private String email;

    private Title title;

    private String department;

    private String collegeCode;

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