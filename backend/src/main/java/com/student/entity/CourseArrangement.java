package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("course_arrangement")
public class CourseArrangement {
    
    @TableId(type = IdType.AUTO)
    private Long id;

    private String arrangementCode;
    
    private Long courseId;
    
    @TableField(exist = false)
    private String courseName;
    
    @TableField(exist = false)
    private BigDecimal credit;
    
    private Long teacherId;
    
    @TableField(exist = false)
    private String teacherName;

    @TableField(exist = false)
    private String teacherNo;
    
    private String classId;
    
    @TableField(exist = false)
    private String className;

    @TableField(exist = false)
    private String classCode;

    @TableField(exist = false)
    private Long collegeId;

    @TableField(exist = false)
    private String collegeName;
    
    private String semester;
    
    private String schedule;
    
    private String room;
    
    private Integer capacity;
    
    private Integer enrolledCount;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    public Integer getAvailableCount() {
        return capacity - enrolledCount;
    }
}
