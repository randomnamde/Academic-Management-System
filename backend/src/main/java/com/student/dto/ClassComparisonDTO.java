package com.student.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ClassComparisonDTO {
    private String semester;
    private String courseName;
    private List<ClassScoreData> classes;

    @Data
    public static class ClassScoreData {
        private String classCode;
        private String className;
        private Integer studentCount;
        private Double averageScore;
        private Double passRate;
        private Double excellentRate;
        private Double maxScore;
        private Double minScore;
        private Integer ranking;
    }
}
