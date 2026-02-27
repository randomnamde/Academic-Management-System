package com.student.dto;

import com.student.entity.Score;
import lombok.Data;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ScoreDTO {
    
    private Long id;
    
    @NotNull(message = "学生ID不能为空")
    private Long studentId;
    
    @NotNull(message = "授课安排ID不能为空")
    private Long courseArrangementId;
    
    private BigDecimal usualScore;
    
    private BigDecimal midtermScore;
    
    private BigDecimal finalScore;
    
    private BigDecimal totalScore;
    
    private BigDecimal gpa;
    
    private LocalDateTime examTime;
    
    private Score.Status status;
    
    private String remark;
}
