package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("mental_interview")
public class MentalInterview {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String studentNo;

    private LocalDateTime interviewDate;

    private String interviewType;

    private String interviewMode;

    private String topic;

    private String content;

    private String emotionState;

    private String riskIndicator;

    private String suggestion;

    private String nextPlan;

    private String interviewerNo;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
