package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.Year;
import java.time.LocalDateTime;

@Data
@TableName("class")
public class Class {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String className;
    
    private String classCode;
    
    private Year grade;

    private String majorCode;

    @TableField(exist = false)
    private String majorName;

    private Long collegeId;
    
    private Long teacherId;
    
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
