package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("mental_crisis")
public class MentalCrisis {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String studentNo;

    private LocalDateTime crisisDate;

    private String crisisType;

    private String riskLevel;

    private String description;

    private String interventionMeasures;

    private String outcome;

    private String relatedPersons;

    private String followUpPlan;

    private String reporterNo;

    private String handlerNo;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
