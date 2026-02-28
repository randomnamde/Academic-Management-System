package com.student.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class DashboardOverviewDTO {

    private String role;

    private Long pendingApprovalCount;

    private Long abnormalTodayCount;

    private Long lowScoreWarningCount;

    private List<TrendPoint> abnormalTrend = new ArrayList<>();

    @Data
    public static class TrendPoint {
        private LocalDate date;
        private Long count;

        public TrendPoint(LocalDate date, Long count) {
            this.date = date;
            this.count = count;
        }
    }
}
