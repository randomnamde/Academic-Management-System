package com.student.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ScoreDistributionDTO {
    private String courseArrangementId;
    private String courseName;
    private String semester;
    private Integer totalStudents;
    private Double averageScore;
    private Double maxScore;
    private Double minScore;
    private Double standardDeviation;
    private List<ScoreRange> distribution;
    private Double passRate;
    private Double excellentRate;
    private Map<String, Object> normalDistribution;

    @Data
    public static class ScoreRange {
        private String range;
        private Integer count;
        private Double percentage;
    }
}
