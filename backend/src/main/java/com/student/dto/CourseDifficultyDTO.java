package com.student.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class CourseDifficultyDTO {
    private String courseCode;
    private String courseName;
    private String semester;
    private Double difficultyIndex;
    private String difficultyLevel;
    private Integer totalStudents;
    private Double averageScore;
    private Double passRate;
    private Double discriminationIndex;
    private Double reliabilityIndex;
    private List<CourseSectionDifficulty> sections;

    @Data
    public static class CourseSectionDifficulty {
        private String sectionName;
        private Double difficultyIndex;
        private Double averageScore;
    }

    public static String calculateDifficultyLevel(Double difficultyIndex) {
        if (difficultyIndex == null) return "未知";
        if (difficultyIndex >= 0.7) return "困难";
        if (difficultyIndex >= 0.5) return "中等";
        if (difficultyIndex >= 0.3) return "简单";
        return "很简��";
    }
}
