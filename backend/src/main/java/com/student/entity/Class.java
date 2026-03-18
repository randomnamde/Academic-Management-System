package com.student.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.Year;

@Data
@TableName("class")
public class Class {

    @TableId(value = "class_code", type = IdType.INPUT)
    private String classCode;

    @TableField("id")
    private Long id;

    private String className;

    private Year grade;

    private String majorCode;

    @TableField(exist = false)
    private String majorName;

    private String collegeCode;

    private String teacherNo;

    @TableField(exist = false)
    private String teacherName;

    private String room;

    private Integer studentCount;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}