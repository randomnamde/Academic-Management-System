package com.student.dto;

import com.student.entity.Score;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ScoreDTO {

    private Long id;

    @NotNull(message = "学生ID不能为空")
    private String studentId;

    @NotNull(message = "授课安排ID不能为空")
    private Long courseArrangementId;

    @DecimalMin(value = "0.0", message = "平时分不能小于0")
    @DecimalMax(value = "100.0", message = "平时分不能大于100")
    private BigDecimal usualScore;

    @DecimalMin(value = "0.0", message = "期中分不能小于0")
    @DecimalMax(value = "100.0", message = "期中分不能大于100")
    private BigDecimal midtermScore;

    @DecimalMin(value = "0.0", message = "期末分不能小于0")
    @DecimalMax(value = "100.0", message = "期末分不能大于100")
    private BigDecimal finalScore;

    private BigDecimal totalScore;

    private BigDecimal gpa;

    private LocalDateTime examTime;

    private Score.Status status;

    private String remark;
}
