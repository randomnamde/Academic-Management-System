package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("course_evaluation")
public class CourseEvaluation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String studentId;

    @TableField(exist = false)
    private String studentName;

    private Long courseArrangementId;

    @TableField(exist = false)
    private String courseName;

    private String teacherNo;

    @TableField(exist = false)
    private String teacherName;

    private BigDecimal teachingScore;

    private BigDecimal contentScore;

    private BigDecimal methodScore;

    private BigDecimal overallScore;

    private String comment;

    private Integer isAnonymous;

    private Status status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public enum Status {
        PENDING, SUBMITTED, PUBLISHED
    }
}
