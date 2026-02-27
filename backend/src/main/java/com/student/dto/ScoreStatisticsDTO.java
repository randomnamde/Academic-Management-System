package com.student.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ScoreStatisticsDTO {
    private Long studentId;
    private String studentName;
    private String studentNo;
    private String className;
    private Double averageScore;
    private BigDecimal gpa;
    private Integer totalCourses;
    private Integer passedCourses;
    private Integer failedCourses;
    private Integer rank;
}
