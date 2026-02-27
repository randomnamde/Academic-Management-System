package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("course")
public class Course {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String courseName;
    
    private String courseCode;
    
    private BigDecimal credit;
    
    private Integer hours;
    
    private Category category;
    
    private String description;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    public enum Category {
        REQUIRED, ELECTIVE, PRACTICAL
    }
}
