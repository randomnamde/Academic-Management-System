package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.AnalyticsFilterDTO;
import com.student.dto.AnalyticsOverviewDTO;
import com.student.dto.RiskStudentDTO;
import com.student.dto.TrendPointDTO;
import com.student.entity.Attendance;
import com.student.entity.CourseArrangement;
import com.student.entity.LeaveRequest;
import com.student.entity.Score;
import com.student.entity.Student;
import com.student.entity.SysUser;
import com.student.exception.BusinessException;
import com.student.mapper.CourseArrangementMapper;
import com.student.mapper.StudentMapper;
import com.student.security.CurrentUserService;
import com.student.service.AnalyticsService;
import com.student.service.AttendanceService;
import com.student.service.LeaveRequestService;
import com.student.service.ScoreService;
import com.student.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final BigDecimal PASS_SCORE = new BigDecimal("60");
    private static final BigDecimal EXCELLENT_SCORE = new BigDecimal("90");
    private static final long OVERDUE_THRESHOLD_HOURS = 48L;

    private final AttendanceService attendanceService;
    private final LeaveRequestService leaveRequestService;
    private final ScoreService scoreService;
    private final StudentService studentService;
    private final CurrentUserService currentUserService;
    private final CourseArrangementMapper courseArrangementMapper;
    private final StudentMapper studentMapper;

    @Override
    public AnalyticsOverviewDTO getOverview(AnalyticsFilterDTO filter, Authentication authentication) {
        DateRange currentRange = normalizeDateRange(filter.getStartDate(), filter.getEndDate());
        DateRange previousRange = previousDateRange(currentRange);

        UserScope scope = resolveUserScope(authentication);
        ArrangementScope arrangementScope = resolveArrangementScope(filter, scope);

        OverviewMetrics current = computeOverviewMetrics(currentRange, scope, arrangementScope);
        OverviewMetrics previous = computeOverviewMetrics(previousRange, scope, arrangementScope);

        AnalyticsOverviewDTO dto = new AnalyticsOverviewDTO();
        dto.setStudentCount(current.studentCount());
        dto.setPendingApprovalCount(current.pendingApprovalCount());
        dto.setLowScoreRiskCount(current.lowScoreRiskCount());
        dto.setAttendanceRate(current.attendanceRate());
        dto.setApprovalAvgHours(current.approvalAvgHours());
        dto.setAttendanceRateChange(calculateChangeRate(current.attendanceRate(), previous.attendanceRate()));
        dto.setApprovalAvgHoursChange(calculateChangeRate(current.approvalAvgHours(), previous.approvalAvgHours()));
        dto.setLowScoreRiskChange(calculateChangeRate(current.lowScoreRiskCount().doubleValue(), previous.lowScoreRiskCount().doubleValue()));
        return dto;
    }

    @Override
    public List<TrendPointDTO> getAttendanceTrend(AnalyticsFilterDTO filter, String granularity, Authentication authentication) {
        DateRange range = normalizeDateRange(filter.getStartDate(), filter.getEndDate());
        UserScope scope = resolveUserScope(authentication);
        ArrangementScope arrangementScope = resolveArrangementScope(filter, scope);
        List<PeriodRange> periods = buildPeriods(range, granularity);

        if (arrangementScope.enabled() && arrangementScope.arrangementIds().isEmpty()) {
            return periods.stream().map(this::emptyTrendPoint).toList();
        }

        List<TrendPointDTO> points = new ArrayList<>();
        for (PeriodRange period : periods) {
            var query = attendanceService.lambdaQuery()
                    .between(Attendance::getAttendanceDate, period.startDate(), period.endDate())
                    .in(Attendance::getStatus, Attendance.Status.ABSENT, Attendance.Status.LATE);
            applyAttendanceUserScope(query, scope);
            applyArrangementScope(query, arrangementScope, Attendance::getCourseArrangementId);

            TrendPointDTO point = new TrendPointDTO();
            point.setPeriodLabel(period.label());
            point.setStartDate(period.startDate());
            point.setEndDate(period.endDate());
            point.setCount(query.count());
            points.add(point);
        }
        return points;
    }

    @Override
    public List<TrendPointDTO> getScoreTrend(AnalyticsFilterDTO filter, String granularity, Authentication authentication) {
        DateRange range = normalizeDateRange(filter.getStartDate(), filter.getEndDate());
        UserScope scope = resolveUserScope(authentication);
        ArrangementScope arrangementScope = resolveArrangementScope(filter, scope);
        List<PeriodRange> periods = buildPeriods(range, granularity);

        if (arrangementScope.enabled() && arrangementScope.arrangementIds().isEmpty()) {
            return periods.stream().map(this::emptyTrendPoint).toList();
        }

        List<TrendPointDTO> points = new ArrayList<>();
        for (PeriodRange period : periods) {
            var query = scoreService.lambdaQuery()
                    .between(Score::getCreateTime, period.startDate().atStartOfDay(), period.endDate().plusDays(1).atStartOfDay().minusNanos(1));
            applyScoreUserScope(query, scope);
            applyArrangementScope(query, arrangementScope, Score::getCourseArrangementId);

            List<Score> scores = query.list();
            long total = scores.size();
            long passed = scores.stream()
                    .map(Score::getTotalScore)
                    .filter(v -> v != null && v.compareTo(PASS_SCORE) >= 0)
                    .count();
            long excellent = scores.stream()
                    .map(Score::getTotalScore)
                    .filter(v -> v != null && v.compareTo(EXCELLENT_SCORE) >= 0)
                    .count();
            double average = scores.stream()
                    .map(Score::getTotalScore)
                    .filter(v -> v != null)
                    .mapToDouble(BigDecimal::doubleValue)
                    .average()
                    .orElse(0D);

            TrendPointDTO point = new TrendPointDTO();
            point.setPeriodLabel(period.label());
            point.setStartDate(period.startDate());
            point.setEndDate(period.endDate());
            point.setCount(total);
            point.setAvgScore(round2(average));
            point.setPassRate(total == 0 ? 0D : round2((double) passed * 100D / total));
            point.setExcellentRate(total == 0 ? 0D : round2((double) excellent * 100D / total));
            points.add(point);
        }
        return points;
    }

    @Override
    public Page<RiskStudentDTO> getRiskStudents(AnalyticsFilterDTO filter,
                                                String riskType,
                                                Integer page,
                                                Integer size,
                                                Authentication authentication) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeSize = size == null || size < 1 ? 10 : Math.min(size, 100);

        DateRange range = normalizeDateRange(filter.getStartDate(), filter.getEndDate());
        UserScope scope = resolveUserScope(authentication);
        ArrangementScope arrangementScope = resolveArrangementScope(filter, scope);
        String normalizedRiskType = normalizeRiskType(riskType);

        List<RiskStudentDTO> allRecords = new ArrayList<>(switch (normalizedRiskType) {
            case "low_score" -> collectLowScoreRisk(range, scope, arrangementScope);
            case "abnormal_attendance" -> collectAbnormalAttendanceRisk(range, scope, arrangementScope);
            case "approval_overdue" -> collectApprovalOverdueRisk(scope, arrangementScope);
            default -> throw new BusinessException(400, "Unsupported riskType: " + normalizedRiskType);
        });

        allRecords.sort(buildRiskComparator(normalizedRiskType));

        long total = allRecords.size();
        int fromIndex = (safePage - 1) * safeSize;
        int toIndex = Math.min(fromIndex + safeSize, allRecords.size());
        List<RiskStudentDTO> pageRecords = fromIndex >= allRecords.size() ? List.of() : allRecords.subList(fromIndex, toIndex);

        Page<RiskStudentDTO> result = new Page<>(safePage, safeSize, total);
        result.setRecords(pageRecords);
        return result;
    }

    private OverviewMetrics computeOverviewMetrics(DateRange range, UserScope scope, ArrangementScope arrangementScope) {
        Long studentCount = countStudents(scope, arrangementScope);
        Long pendingApproval = countPendingApprovals(scope, arrangementScope);
        Long lowScoreRisk = countLowScoreRisk(range, scope, arrangementScope);
        Double attendanceRate = calculateAttendanceRate(range, scope, arrangementScope);
        Double approvalAvgHours = calculateApprovalAverageHours(range, scope, arrangementScope);
        return new OverviewMetrics(studentCount, pendingApproval, lowScoreRisk, attendanceRate, approvalAvgHours);
    }

    private Long countStudents(UserScope scope, ArrangementScope arrangementScope) {
        if (scope.role() == SysUser.Role.STUDENT) {
            return 1L;
        }
        if (arrangementScope.enabled()) {
            if (arrangementScope.arrangementIds().isEmpty()) {
                return 0L;
            }
            List<CourseArrangement> arrangements = courseArrangementMapper.selectList(
                    new LambdaQueryWrapper<CourseArrangement>().in(CourseArrangement::getId, arrangementScope.arrangementIds()));
            Set<Long> classIds = new HashSet<>();
            for (CourseArrangement arrangement : arrangements) {
                if (arrangement.getClassId() != null) {
                    classIds.add(arrangement.getClassId());
                }
            }
            if (classIds.isEmpty()) {
                return 0L;
            }
            return studentService.lambdaQuery().in(Student::getClassId, classIds).count();
        }
        return studentService.lambdaQuery().count();
    }

    private Long countPendingApprovals(UserScope scope, ArrangementScope arrangementScope) {
        var query = leaveRequestService.lambdaQuery();
        if (scope.role() == SysUser.Role.STUDENT) {
            query.in(LeaveRequest::getStatus, LeaveRequest.Status.PENDING, LeaveRequest.Status.REJECTED);
        } else {
            query.eq(LeaveRequest::getStatus, LeaveRequest.Status.PENDING);
        }
        applyLeaveUserScope(query, scope);
        if (arrangementScope.enabled()) {
            if (arrangementScope.arrangementIds().isEmpty()) {
                return 0L;
            }
            query.in(LeaveRequest::getCourseArrangementId, arrangementScope.arrangementIds());
        }
        return query.count();
    }

    private Long countLowScoreRisk(DateRange range, UserScope scope, ArrangementScope arrangementScope) {
        if (arrangementScope.enabled() && arrangementScope.arrangementIds().isEmpty()) {
            return 0L;
        }
        var query = scoreService.lambdaQuery()
                .lt(Score::getTotalScore, PASS_SCORE)
                .between(Score::getCreateTime, range.startDate().atStartOfDay(), range.endDate().plusDays(1).atStartOfDay().minusNanos(1));
        applyScoreUserScope(query, scope);
        applyArrangementScope(query, arrangementScope, Score::getCourseArrangementId);
        if (scope.role() != SysUser.Role.STUDENT) {
            return query.count();
        }

        List<Score> lowScores = query.list();
        if (lowScores.isEmpty()) {
            return 0L;
        }
        Set<Long> arrangementIds = new HashSet<>();
        for (Score score : lowScores) {
            if (score.getCourseArrangementId() != null) {
                arrangementIds.add(score.getCourseArrangementId());
            }
        }
        if (arrangementIds.isEmpty()) {
            return 0L;
        }

        List<CourseArrangement> arrangements = courseArrangementMapper.selectList(
                new LambdaQueryWrapper<CourseArrangement>().in(CourseArrangement::getId, arrangementIds));
        Set<Long> courseIds = new HashSet<>();
        for (CourseArrangement arrangement : arrangements) {
            if (arrangement.getCourseId() != null) {
                courseIds.add(arrangement.getCourseId());
            }
        }
        return (long) courseIds.size();
    }

    private Double calculateAttendanceRate(DateRange range, UserScope scope, ArrangementScope arrangementScope) {
        if (arrangementScope.enabled() && arrangementScope.arrangementIds().isEmpty()) {
            return 0D;
        }
        var totalQuery = attendanceService.lambdaQuery()
                .between(Attendance::getAttendanceDate, range.startDate(), range.endDate());
        applyAttendanceUserScope(totalQuery, scope);
        applyArrangementScope(totalQuery, arrangementScope, Attendance::getCourseArrangementId);
        long total = totalQuery.count();
        if (total == 0) {
            return 0D;
        }

        var presentQuery = attendanceService.lambdaQuery()
                .between(Attendance::getAttendanceDate, range.startDate(), range.endDate())
                .eq(Attendance::getStatus, Attendance.Status.PRESENT);
        applyAttendanceUserScope(presentQuery, scope);
        applyArrangementScope(presentQuery, arrangementScope, Attendance::getCourseArrangementId);
        long presentCount = presentQuery.count();
        return round2((double) presentCount * 100D / total);
    }

    private Double calculateApprovalAverageHours(DateRange range, UserScope scope, ArrangementScope arrangementScope) {
        if (arrangementScope.enabled() && arrangementScope.arrangementIds().isEmpty()) {
            return 0D;
        }
        var query = leaveRequestService.lambdaQuery()
                .in(LeaveRequest::getStatus, LeaveRequest.Status.APPROVED, LeaveRequest.Status.REJECTED)
                .isNotNull(LeaveRequest::getApproveTime)
                .between(LeaveRequest::getCreateTime, range.startDate().atStartOfDay(), range.endDate().plusDays(1).atStartOfDay().minusNanos(1));
        applyLeaveUserScope(query, scope);
        applyArrangementScope(query, arrangementScope, LeaveRequest::getCourseArrangementId);
        List<LeaveRequest> records = query.list();
        if (records.isEmpty()) {
            return 0D;
        }
        double averageHours = records.stream()
                .filter(item -> item.getApproveTime() != null && item.getCreateTime() != null)
                .mapToLong(item -> Duration.between(item.getCreateTime(), item.getApproveTime()).toHours())
                .average()
                .orElse(0D);
        return round2(averageHours);
    }

    private List<RiskStudentDTO> collectLowScoreRisk(DateRange range, UserScope scope, ArrangementScope arrangementScope) {
        if (arrangementScope.enabled() && arrangementScope.arrangementIds().isEmpty()) {
            return List.of();
        }
        var query = scoreService.lambdaQuery()
                .lt(Score::getTotalScore, PASS_SCORE)
                .between(Score::getCreateTime, range.startDate().atStartOfDay(), range.endDate().plusDays(1).atStartOfDay().minusNanos(1));
        applyScoreUserScope(query, scope);
        applyArrangementScope(query, arrangementScope, Score::getCourseArrangementId);
        List<Score> records = query.list();

        Map<Long, Aggregate> aggregateMap = new HashMap<>();
        for (Score score : records) {
            if (score.getStudentId() == null) {
                continue;
            }
            double value = score.getTotalScore() == null ? 0D : score.getTotalScore().doubleValue();
            aggregateMap.computeIfAbsent(score.getStudentId(), key -> new Aggregate()).add(value);
        }
        return toRiskStudentList(aggregateMap, "low_score", true);
    }

    private List<RiskStudentDTO> collectAbnormalAttendanceRisk(DateRange range, UserScope scope, ArrangementScope arrangementScope) {
        if (arrangementScope.enabled() && arrangementScope.arrangementIds().isEmpty()) {
            return List.of();
        }
        var query = attendanceService.lambdaQuery()
                .between(Attendance::getAttendanceDate, range.startDate(), range.endDate())
                .in(Attendance::getStatus, Attendance.Status.ABSENT, Attendance.Status.LATE);
        applyAttendanceUserScope(query, scope);
        applyArrangementScope(query, arrangementScope, Attendance::getCourseArrangementId);
        List<Attendance> records = query.list();

        Map<Long, Aggregate> aggregateMap = new HashMap<>();
        for (Attendance attendance : records) {
            if (attendance.getStudentId() == null) {
                continue;
            }
            aggregateMap.computeIfAbsent(attendance.getStudentId(), key -> new Aggregate()).add(1D);
        }
        return toRiskStudentList(aggregateMap, "abnormal_attendance", false);
    }

    private List<RiskStudentDTO> collectApprovalOverdueRisk(UserScope scope, ArrangementScope arrangementScope) {
        if (arrangementScope.enabled() && arrangementScope.arrangementIds().isEmpty()) {
            return List.of();
        }
        LocalDateTime threshold = LocalDateTime.now().minusHours(OVERDUE_THRESHOLD_HOURS);
        var query = leaveRequestService.lambdaQuery()
                .eq(LeaveRequest::getStatus, LeaveRequest.Status.PENDING)
                .le(LeaveRequest::getCreateTime, threshold);
        applyLeaveUserScope(query, scope);
        applyArrangementScope(query, arrangementScope, LeaveRequest::getCourseArrangementId);
        List<LeaveRequest> records = query.list();

        Map<Long, Aggregate> aggregateMap = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        for (LeaveRequest leaveRequest : records) {
            if (leaveRequest.getStudentId() == null || leaveRequest.getCreateTime() == null) {
                continue;
            }
            double overdueHours = Duration.between(leaveRequest.getCreateTime(), now).toHours();
            aggregateMap.computeIfAbsent(leaveRequest.getStudentId(), key -> new Aggregate()).add(overdueHours);
        }
        return toRiskStudentList(aggregateMap, "approval_overdue", true);
    }

    private List<RiskStudentDTO> toRiskStudentList(Map<Long, Aggregate> aggregateMap, String riskType, boolean averageValue) {
        if (aggregateMap.isEmpty()) {
            return List.of();
        }
        List<RiskStudentDTO> list = new ArrayList<>();
        for (Map.Entry<Long, Aggregate> entry : aggregateMap.entrySet()) {
            Student student = studentMapper.selectByIdWithClass(entry.getKey());
            if (student == null) {
                continue;
            }
            Aggregate aggregate = entry.getValue();
            RiskStudentDTO dto = new RiskStudentDTO();
            dto.setStudentId(student.getId());
            dto.setStudentNo(student.getStudentNo());
            dto.setStudentName(student.getName());
            dto.setClassName(student.getClassName());
            dto.setRiskType(riskType);
            dto.setRiskCount(aggregate.count);
            double value = averageValue ? aggregate.average() : aggregate.total;
            dto.setRiskValue(round2(value));
            list.add(dto);
        }
        return list;
    }

    private TrendPointDTO emptyTrendPoint(PeriodRange period) {
        TrendPointDTO point = new TrendPointDTO();
        point.setPeriodLabel(period.label());
        point.setStartDate(period.startDate());
        point.setEndDate(period.endDate());
        point.setCount(0L);
        point.setAvgScore(0D);
        point.setPassRate(0D);
        point.setExcellentRate(0D);
        return point;
    }

    private Comparator<RiskStudentDTO> buildRiskComparator(String riskType) {
        Comparator<RiskStudentDTO> byCount = Comparator.comparing(RiskStudentDTO::getRiskCount, Comparator.nullsLast(Long::compareTo)).reversed();
        if ("low_score".equals(riskType)) {
            return byCount.thenComparing(RiskStudentDTO::getRiskValue, Comparator.nullsLast(Double::compareTo));
        }
        return byCount.thenComparing(RiskStudentDTO::getRiskValue, Comparator.nullsLast(Double::compareTo)).reversed();
    }

    private UserScope resolveUserScope(Authentication authentication) {
        SysUser currentUser = currentUserService.getCurrentUser(authentication);
        if (currentUser.getRole() == SysUser.Role.STUDENT) {
            Student student = currentUserService.getCurrentStudent(authentication);
            return new UserScope(currentUser.getRole(), student.getId(), null, student.getClassId());
        }
        if (currentUser.getRole() == SysUser.Role.TEACHER) {
            Long teacherId = currentUserService.getCurrentTeacherId(authentication);
            return new UserScope(currentUser.getRole(), null, teacherId, null);
        }
        return new UserScope(currentUser.getRole(), null, null, null);
    }

    private ArrangementScope resolveArrangementScope(AnalyticsFilterDTO filter, UserScope scope) {
        Long teacherId = null;
        Long classId = null;
        if (scope.role() == SysUser.Role.TEACHER) {
            teacherId = scope.teacherId();
            classId = filter.getClassId();
        } else if (scope.role() == SysUser.Role.ADMIN) {
            teacherId = filter.getTeacherId();
            classId = filter.getClassId();
        }
        String semester = filter.getSemester();

        // Student analytics must stay in "self" scope; classId/teacherId filters are ignored.
        // Optional semester filter is still supported for narrowing personal records.
        boolean enabled;
        if (scope.role() == SysUser.Role.ADMIN) {
            enabled = teacherId != null || classId != null || StringUtils.hasText(semester);
        } else if (scope.role() == SysUser.Role.TEACHER) {
            enabled = true;
        } else {
            enabled = StringUtils.hasText(semester);
        }
        if (!enabled) {
            return new ArrangementScope(false, Set.of());
        }

        var query = new LambdaQueryWrapper<CourseArrangement>();
        if (teacherId != null) {
            query.eq(CourseArrangement::getTeacherId, teacherId);
        }
        if (classId != null) {
            query.eq(CourseArrangement::getClassId, classId);
        }
        if (StringUtils.hasText(semester)) {
            query.eq(CourseArrangement::getSemester, semester.trim());
        }

        List<CourseArrangement> arrangements = courseArrangementMapper.selectList(query);
        Set<Long> ids = new HashSet<>();
        for (CourseArrangement arrangement : arrangements) {
            if (arrangement.getId() != null) {
                ids.add(arrangement.getId());
            }
        }
        return new ArrangementScope(true, ids);
    }

    private DateRange normalizeDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDate end = endDate == null ? LocalDate.now() : endDate;
        LocalDate start = startDate == null ? end.minusDays(29) : startDate;
        if (start.isAfter(end)) {
            throw new BusinessException(400, "startDate cannot be after endDate");
        }
        return new DateRange(start, end);
    }

    private DateRange previousDateRange(DateRange current) {
        long days = Duration.between(current.startDate().atStartOfDay(), current.endDate().plusDays(1).atStartOfDay()).toDays();
        LocalDate previousEnd = current.startDate().minusDays(1);
        LocalDate previousStart = previousEnd.minusDays(days - 1);
        return new DateRange(previousStart, previousEnd);
    }

    private List<PeriodRange> buildPeriods(DateRange range, String granularityRaw) {
        String granularity = StringUtils.hasText(granularityRaw) ? granularityRaw.trim().toLowerCase() : "day";
        if (!List.of("day", "week", "month").contains(granularity)) {
            throw new BusinessException(400, "granularity must be one of: day, week, month");
        }
        List<PeriodRange> periods = new ArrayList<>();
        if ("day".equals(granularity)) {
            LocalDate cursor = range.startDate();
            while (!cursor.isAfter(range.endDate())) {
                periods.add(new PeriodRange(cursor, cursor, cursor.toString()));
                cursor = cursor.plusDays(1);
            }
            return periods;
        }

        if ("week".equals(granularity)) {
            LocalDate cursor = range.startDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            while (!cursor.isAfter(range.endDate())) {
                LocalDate start = cursor.isBefore(range.startDate()) ? range.startDate() : cursor;
                LocalDate end = cursor.plusDays(6);
                if (end.isAfter(range.endDate())) {
                    end = range.endDate();
                }
                periods.add(new PeriodRange(start, end, start + " ~ " + end));
                cursor = cursor.plusWeeks(1);
            }
            return periods;
        }

        LocalDate cursor = range.startDate().withDayOfMonth(1);
        while (!cursor.isAfter(range.endDate())) {
            LocalDate start = cursor.isBefore(range.startDate()) ? range.startDate() : cursor;
            LocalDate end = cursor.withDayOfMonth(cursor.lengthOfMonth());
            if (end.isAfter(range.endDate())) {
                end = range.endDate();
            }
            periods.add(new PeriodRange(start, end, start.getYear() + "-" + String.format("%02d", start.getMonthValue())));
            cursor = cursor.plusMonths(1).withDayOfMonth(1);
        }
        return periods;
    }

    private String normalizeRiskType(String riskTypeRaw) {
        String riskType = StringUtils.hasText(riskTypeRaw) ? riskTypeRaw.trim().toLowerCase() : "low_score";
        if (!List.of("low_score", "abnormal_attendance", "approval_overdue").contains(riskType)) {
            throw new BusinessException(400, "riskType must be one of: low_score, abnormal_attendance, approval_overdue");
        }
        return riskType;
    }

    private <T> void applyArrangementScope(com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper<T> query,
                                           ArrangementScope arrangementScope,
                                           com.baomidou.mybatisplus.core.toolkit.support.SFunction<T, Long> arrangementField) {
        if (!arrangementScope.enabled()) {
            return;
        }
        Collection<Long> arrangementIds = arrangementScope.arrangementIds();
        if (arrangementIds.isEmpty()) {
            query.eq(arrangementField, -1L);
            return;
        }
        query.in(arrangementField, arrangementIds);
    }

    private void applyScoreUserScope(com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper<Score> query, UserScope scope) {
        if (scope.role() == SysUser.Role.STUDENT && scope.studentId() != null) {
            query.eq(Score::getStudentId, scope.studentId());
        }
    }

    private void applyAttendanceUserScope(com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper<Attendance> query, UserScope scope) {
        if (scope.role() == SysUser.Role.STUDENT && scope.studentId() != null) {
            query.eq(Attendance::getStudentId, scope.studentId());
        }
    }

    private void applyLeaveUserScope(com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper<LeaveRequest> query, UserScope scope) {
        if (scope.role() == SysUser.Role.STUDENT && scope.studentId() != null) {
            query.eq(LeaveRequest::getStudentId, scope.studentId());
        }
    }

    private double calculateChangeRate(double current, double previous) {
        if (previous == 0D) {
            return current == 0D ? 0D : 100D;
        }
        return round2((current - previous) * 100D / previous);
    }

    private double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private record UserScope(SysUser.Role role, Long studentId, Long teacherId, Long classId) {
    }

    private record ArrangementScope(boolean enabled, Set<Long> arrangementIds) {
    }

    private record DateRange(LocalDate startDate, LocalDate endDate) {
    }

    private record PeriodRange(LocalDate startDate, LocalDate endDate, String label) {
    }

    private record OverviewMetrics(Long studentCount,
                                   Long pendingApprovalCount,
                                   Long lowScoreRiskCount,
                                   Double attendanceRate,
                                   Double approvalAvgHours) {
    }

    private static class Aggregate {
        private long count = 0;
        private double total = 0;

        private void add(double value) {
            this.count++;
            this.total += value;
        }

        private double average() {
            if (count == 0) {
                return 0D;
            }
            return total / count;
        }
    }
}
