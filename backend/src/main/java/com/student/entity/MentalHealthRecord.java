package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("mental_health_record")
public class MentalHealthRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String studentNo;

    private LocalDate recordDate;

    private String assessmentType;

    private BigDecimal totalScore;

    private BigDecimal somaticScore;

    private BigDecimal obsessiveScore;

    private BigDecimal interpersonalScore;

    private BigDecimal depressionScore;

    private BigDecimal anxietyScore;

    private BigDecimal hostilityScore;

    private BigDecimal terrorScore;

    private BigDecimal paranoidScore;

    private BigDecimal psychoticismScore;

    private String riskLevel;

    private String counselorNote;

    private String followUpStatus;

    private LocalDate followUpDate;

    private String assessorNo;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
