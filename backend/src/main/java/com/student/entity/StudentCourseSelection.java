package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("student_course_selection")
public class StudentCourseSelection {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long studentId;
    
    private Long courseArrangementId;
    
    private String status; // PENDING, SUCCESS, FAILED, DROPPED
    
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
