package com.student.dto;

import lombok.Data;

@Data
public class AnalyticsOverviewDTO {

    private Long studentCount;

    private Long pendingApprovalCount;

    private Long lowScoreRiskCount;

    private Double attendanceRate;

    private Double approvalAvgHours;

    private Double attendanceRateChange;

    private Double approvalAvgHoursChange;

    private Double lowScoreRiskChange;
}

