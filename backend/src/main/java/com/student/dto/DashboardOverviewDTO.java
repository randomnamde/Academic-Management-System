package com.student.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class DashboardOverviewDTO {

    private String role;

    private Long studentCount;

    private Long teacherCount;

    private Long courseCount;

    private Long classCount;

    private GenderStatistics genderStatistics = new GenderStatistics();

    private CourseCategoryStatistics courseCategoryStatistics = new CourseCategoryStatistics();

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

    @Data
    public static class GenderStatistics {
        private Long male = 0L;
        private Long female = 0L;
    }

    @Data
    public static class CourseCategoryStatistics {
        private Long required = 0L;
        private Long elective = 0L;
        private Long practical = 0L;
    }
}
