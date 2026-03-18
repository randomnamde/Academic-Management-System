package com.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

public interface AnalyticsService {

    AnalyticsOverviewDTO getOverview(AnalyticsFilterDTO filter, Authentication authentication);

    List<TrendPointDTO> getAttendanceTrend(AnalyticsFilterDTO filter, String granularity, Authentication authentication);

    List<TrendPointDTO> getScoreTrend(AnalyticsFilterDTO filter, String granularity, Authentication authentication);

    Page<RiskStudentDTO> getRiskStudents(AnalyticsFilterDTO filter,
                                         String riskType,
                                         Integer page,
                                         Integer size,
                                         Authentication authentication);

    ScoreDistributionDTO getScoreDistribution(Long courseArrangementId, String semester, String classCode, Authentication authentication);

    ClassComparisonDTO getClassComparison(String semester, Long courseArrangementId, String collegeCode, Authentication authentication);

    List<CourseDifficultyDTO> getCourseDifficulty(String semester, String collegeCode, String courseCode, Authentication authentication);

    List<Map<String, Object>> getScoreRank(String semester, String classCode, Long courseArrangementId, Integer topN, Authentication authentication);
}

