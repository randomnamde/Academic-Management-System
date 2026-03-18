package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.*;
import com.student.service.AnalyticsService;
import com.student.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<AnalyticsOverviewDTO> overview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String classId,
            @RequestParam(required = false) String teacherNo,
            Authentication authentication) {
        AnalyticsFilterDTO filter = buildFilter(startDate, endDate, semester, classId, teacherNo);
        return ResultVO.success(analyticsService.getOverview(filter, authentication));
    }

    @GetMapping("/attendance-trend")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<List<TrendPointDTO>> attendanceTrend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String classId,
            @RequestParam(required = false) String teacherNo,
            @RequestParam(defaultValue = "day") String granularity,
            Authentication authentication) {
        AnalyticsFilterDTO filter = buildFilter(startDate, endDate, semester, classId, teacherNo);
        return ResultVO.success(analyticsService.getAttendanceTrend(filter, granularity, authentication));
    }

    @GetMapping("/score-trend")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<List<TrendPointDTO>> scoreTrend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String classId,
            @RequestParam(required = false) String teacherNo,
            @RequestParam(defaultValue = "day") String granularity,
            Authentication authentication) {
        AnalyticsFilterDTO filter = buildFilter(startDate, endDate, semester, classId, teacherNo);
        return ResultVO.success(analyticsService.getScoreTrend(filter, granularity, authentication));
    }

    @GetMapping("/risk-students")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<Page<RiskStudentDTO>> riskStudents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String classId,
            @RequestParam(required = false) String teacherNo,
            @RequestParam(defaultValue = "low_score") String riskType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Authentication authentication) {
        AnalyticsFilterDTO filter = buildFilter(startDate, endDate, semester, classId, teacherNo);
        return ResultVO.success(analyticsService.getRiskStudents(filter, riskType, page, size, authentication));
    }

    private AnalyticsFilterDTO buildFilter(LocalDate startDate,
                                           LocalDate endDate,
                                           String semester,
                                           String classId,
                                           String teacherNo) {
        AnalyticsFilterDTO filter = new AnalyticsFilterDTO();
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setSemester(semester);
        filter.setClassId(classId);
        filter.setTeacherNo(teacherNo);
        return filter;
    }

    @GetMapping("/score-distribution")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<ScoreDistributionDTO> scoreDistribution(
            @RequestParam(required = false) Long courseArrangementId,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String classCode,
            Authentication authentication) {
        return ResultVO.success(analyticsService.getScoreDistribution(courseArrangementId, semester, classCode, authentication));
    }

    @GetMapping("/class-comparison")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER')")
    public ResultVO<ClassComparisonDTO> classComparison(
            @RequestParam String semester,
            @RequestParam(required = false) Long courseArrangementId,
            @RequestParam(required = false) String collegeCode,
            Authentication authentication) {
        return ResultVO.success(analyticsService.getClassComparison(semester, courseArrangementId, collegeCode, authentication));
    }

    @GetMapping("/course-difficulty")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<List<CourseDifficultyDTO>> courseDifficulty(
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String collegeCode,
            @RequestParam(required = false) String courseCode,
            Authentication authentication) {
        return ResultVO.success(analyticsService.getCourseDifficulty(semester, collegeCode, courseCode, authentication));
    }

    @GetMapping("/score-rank")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<List<Map<String, Object>>> scoreRank(
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String classCode,
            @RequestParam(required = false) Long courseArrangementId,
            @RequestParam(defaultValue = "10") Integer topN,
            Authentication authentication) {
        return ResultVO.success(analyticsService.getScoreRank(semester, classCode, courseArrangementId, topN, authentication));
    }
}




