package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.AnalyticsFilterDTO;
import com.student.dto.AnalyticsOverviewDTO;
import com.student.dto.RiskStudentDTO;
import com.student.dto.TrendPointDTO;
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

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<AnalyticsOverviewDTO> overview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long teacherId,
            Authentication authentication) {
        AnalyticsFilterDTO filter = buildFilter(startDate, endDate, semester, classId, teacherId);
        return ResultVO.success(analyticsService.getOverview(filter, authentication));
    }

    @GetMapping("/attendance-trend")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<List<TrendPointDTO>> attendanceTrend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(defaultValue = "day") String granularity,
            Authentication authentication) {
        AnalyticsFilterDTO filter = buildFilter(startDate, endDate, semester, classId, teacherId);
        return ResultVO.success(analyticsService.getAttendanceTrend(filter, granularity, authentication));
    }

    @GetMapping("/score-trend")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<List<TrendPointDTO>> scoreTrend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(defaultValue = "day") String granularity,
            Authentication authentication) {
        AnalyticsFilterDTO filter = buildFilter(startDate, endDate, semester, classId, teacherId);
        return ResultVO.success(analyticsService.getScoreTrend(filter, granularity, authentication));
    }

    @GetMapping("/risk-students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<Page<RiskStudentDTO>> riskStudents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(defaultValue = "low_score") String riskType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Authentication authentication) {
        AnalyticsFilterDTO filter = buildFilter(startDate, endDate, semester, classId, teacherId);
        return ResultVO.success(analyticsService.getRiskStudents(filter, riskType, page, size, authentication));
    }

    private AnalyticsFilterDTO buildFilter(LocalDate startDate,
                                           LocalDate endDate,
                                           String semester,
                                           Long classId,
                                           Long teacherId) {
        AnalyticsFilterDTO filter = new AnalyticsFilterDTO();
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setSemester(semester);
        filter.setClassId(classId);
        filter.setTeacherId(teacherId);
        return filter;
    }
}

