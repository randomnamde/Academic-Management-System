package com.student.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import java.math.BigDecimal;

@Data
public class CourseEvaluationDTO {

    private Long id;

    @NotNull(message = "学生ID不能为空")
    private String studentId;

    @NotNull(message = "授课安排ID不能为空")
    private Long courseArrangementId;

    @NotNull(message = "教师编号不能为空")
    private String teacherNo;

    @DecimalMin(value = "0.0", message = "教学评分不能小于0")
    @DecimalMax(value = "5.0", message = "教学评分不能大于5")
    private BigDecimal teachingScore;

    @DecimalMin(value = "0.0", message = "内容评分不能小于0")
    @DecimalMax(value = "5.0", message = "内容评分不能大于5")
    private BigDecimal contentScore;

    @DecimalMin(value = "0.0", message = "方法评分不能小于0")
    @DecimalMax(value = "5.0", message = "方法评分不能大于5")
    private BigDecimal methodScore;

    private BigDecimal overallScore;

    private String comment;

    private Integer isAnonymous;

    private String status;
}
