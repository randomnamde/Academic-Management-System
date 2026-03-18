package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.*;
import com.student.dto.AnalyticsFilterDTO;
import com.student.dto.AnalyticsOverviewDTO;
import com.student.dto.RiskStudentDTO;
import com.student.dto.TrendPointDTO;
import com.student.entity.Attendance;
import com.student.entity.Class;
import com.student.entity.Course;
import com.student.entity.CourseArrangement;
import com.student.entity.LeaveRequest;
import com.student.entity.Score;
import com.student.entity.Student;
import com.student.entity.SysUser;
import com.student.exception.BusinessException;
import com.student.mapper.CourseMapper;
import com.student.mapper.CourseArrangementMapper;
import com.student.mapper.StudentMapper;
import com.student.mapper.ClassMapper;
import com.student.security.CurrentUserService;
import com.student.security.RoleCode;
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
    private final CourseMapper courseMapper;
    private final CourseArrangementMapper courseArrangementMapper;
    private final StudentMapper studentMapper;
    private final ClassMapper classMapper;

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

        List<RiskStudentDTO> allRecords;
        if (scope.role().isStudent()) {
            allRecords = new ArrayList<>(switch (normalizedRiskType) {
                case "low_score" -> collectStudentLowScoreRiskDetails(range, scope, arrangementScope);
                case "abnormal_attendance" -> collectStudentAbnormalAttendanceRiskDetails(range, scope, arrangementScope);
                case "approval_overdue" -> collectStudentApprovalOverdueRiskDetails(scope, arrangementScope);
                default -> throw new BusinessException(400, "Unsupported riskType: " + normalizedRiskType);
            });
            allRecords.sort(buildStudentRiskComparator(normalizedRiskType));
        } else {
            allRecords = new ArrayList<>(switch (normalizedRiskType) {
                case "low_score" -> collectLowScoreRisk(range, scope, arrangementScope);
                case "abnormal_attendance" -> collectAbnormalAttendanceRisk(range, scope, arrangementScope);
                case "approval_overdue" -> collectApprovalOverdueRisk(scope, arrangementScope);
                default -> throw new BusinessException(400, "Unsupported riskType: " + normalizedRiskType);
            });
            allRecords.sort(buildRiskComparator(normalizedRiskType));
        }

        long total = allRecords.size();
        int fromIndex = (safePage - 1) * safeSize;
        int toIndex = Math.min(fromIndex + safeSize, allRecords.size());
        List<RiskStudentDTO> pageRecords = fromIndex >= allRecords.size() ? List.of() : allRecords.subList(fromIndex, toIndex);

        Page<RiskStudentDTO> result = new Page<>(safePage, safeSize, total);
        result.setRecords(pageRecords);
        return result;
    }

    @Override
    public ScoreDistributionDTO getScoreDistribution(Long courseArrangementId, String semester, String classCode, Authentication authentication) {
        UserScope scope = resolveUserScope(authentication);

        // Build query
        var query = scoreService.lambdaQuery();
        query.select(Score::getTotalScore);
        query.join(Score.class, Score::getCourseArrangementId, CourseArrangement.class, Score::getCourseArrangementId);
        query.leftJoin(Student.class, Score::getStudentId, Student::getStudentNo);

        if (courseArrangementId != null) {
            query.eq(Score::getCourseArrangementId, courseArrangementId);
        }
        if (StringUtils.hasText(semester)) {
            query.eq(CourseArrangement::getSemester, semester);
        }
        if (StringUtils.hasText(classCode)) {
            query.eq(Student::getClassCode, classCode);
        }

        // Apply data scope
        if (scope.role().isStudent()) {
            query.eq(Score::getStudentId, scope.userNo());
        } else if (scope.role().isTeacher()) {
            query.eq(CourseArrangement::getTeacherNo, scope.userNo());
        }

        List<Score> scores = query.list();

        ScoreDistributionDTO dto = new ScoreDistributionDTO();
        if (scores.isEmpty()) {
            dto.setTotalStudents(0);
            dto.setAverageScore(0.0);
            return dto;
        }

        List<Double> scoreList = scores.stream()
                .map(Score::getTotalScore)
                .filter(s -> s != null)
                .map(BigDecimal::doubleValue)
                .toList();

        dto.setTotalStudents(scoreList.size());

        // Calculate statistics
        double avg = scoreList.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double max = scoreList.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double min = scoreList.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double stdDev = calculateStandardDeviation(scoreList, avg);

        dto.setAverageScore(round2(avg));
        dto.setMaxScore(max);
        dto.setMinScore(min);
        dto.setStandardDeviation(round2(stdDev));

        // Distribution
        List<ScoreDistributionDTO.ScoreRange> distribution = calculateDistribution(scoreList);
        dto.setDistribution(distribution);

        // Pass rate
        long passCount = scoreList.stream().filter(s -> s >= 60).count();
        long excellentCount = scoreList.stream().filter(s -> s >= 90).count();
        dto.setPassRate(round2(100.0 * passCount / scoreList.size()));
        dto.setExcellentRate(round2(100.0 * excellentCount / scoreList.size()));

        return dto;
    }

    @Override
    public ClassComparisonDTO getClassComparison(String semester, Long courseArrangementId, String collegeCode, Authentication authentication) {
        ClassComparisonDTO dto = new ClassComparisonDTO();
        dto.setSemester(semester);

        // Query scores grouped by class
        var scoreQuery = scoreService.lambdaQuery();
        scoreQuery.select(Score::getStudentId, Score::getTotalScore);
        scoreQuery.join(Score.class, Score::getCourseArrangementId, CourseArrangement.class, Score::getCourseArrangementId);
        scoreQuery.leftJoin(Student.class, Score::getStudentId, Student::getStudentNo);
        scoreQuery.leftJoin(Class.class, Student::getClassCode, Class::getClassCode);

        if (StringUtils.hasText(semester)) {
            scoreQuery.eq(CourseArrangement::getSemester, semester);
        }
        if (courseArrangementId != null) {
            scoreQuery.eq(Score::getCourseArrangementId, courseArrangementId);
        }
        if (StringUtils.hasText(collegeCode)) {
            scoreQuery.eq(Class::getCollegeCode, collegeCode);
        }

        List<Score> scores = scoreQuery.list();

        // Group by class
        Map<String, List<Score>> byClass = scores.stream()
                .filter(s -> s.getStudentId() != null)
                .collect(java.util.stream.Collectors.groupingBy(Score::getStudentId));

        // Get class info
        List<Class> classes = classMapper.selectList(null);
        Map<String, Class> classMap = classes.stream()
                .collect(java.util.stream.Collectors.toMap(Class::getClassCode, c -> c));

        // Build comparison data
        List<ClassComparisonDTO.ClassScoreData> classDataList = new ArrayList<>();
        int ranking = 1;

        for (Map.Entry<String, List<Score>> entry : byClass.entrySet()) {
            String studentNo = entry.getKey();
            Student student = studentMapper.selectByStudentNo(studentNo);
            if (student == null || student.getClassCode() == null) continue;

            Class cls = classMap.get(student.getClassCode());
            if (cls == null) continue;

            List<Score> classScores = entry.getValue();
            List<Double> scoreValues = classScores.stream()
                    .map(Score::getTotalScore)
                    .filter(s -> s != null)
                    .map(BigDecimal::doubleValue)
                    .toList();

            if (scoreValues.isEmpty()) continue;

            double avg = scoreValues.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            long passCount = scoreValues.stream().filter(s -> s >= 60).count();
            long excellentCount = scoreValues.stream().filter(s -> s >= 90).count();

            ClassComparisonDTO.ClassScoreData data = new ClassComparisonDTO.ClassScoreData();
            data.setClassCode(cls.getClassCode());
            data.setClassName(cls.getClassName());
            data.setStudentCount(classScores.size());
            data.setAverageScore(round2(avg));
            data.setPassRate(round2(100.0 * passCount / classScores.size()));
            data.setExcellentRate(round2(100.0 * excellentCount / classScores.size()));
            data.setMaxScore(scoreValues.stream().mapToDouble(Double::doubleValue).max().orElse(0));
            data.setMinScore(scoreValues.stream().mapToDouble(Double::doubleValue).min().orElse(0));
            data.setRanking(ranking++);
            classDataList.add(data);
        }

        // Sort by average score
        classDataList.sort((a, b) -> Double.compare(b.getAverageScore(), a.getAverageScore()));
        for (int i = 0; i < classDataList.size(); i++) {
            classDataList.get(i).setRanking(i + 1);
        }

        dto.setClasses(classDataList);
        return dto;
    }

    @Override
    public List<CourseDifficultyDTO> getCourseDifficulty(String semester, String collegeCode, String courseCode, Authentication authentication) {
        UserScope scope = resolveUserScope(authentication);

        // Query course arrangements
        var caQuery = new LambdaQueryWrapper<CourseArrangement>();
        if (StringUtils.hasText(semester)) {
            caQuery.eq(CourseArrangement::getSemester, semester);
        }
        if (StringUtils.hasText(collegeCode)) {
            caQuery.eq(CourseArrangement::getCollegeCode, collegeCode);
        }
        if (StringUtils.hasText(courseCode)) {
            caQuery.eq(CourseArrangement::getCourseCode, courseCode);
        }
        if (scope.role().isTeacher()) {
            caQuery.eq(CourseArrangement::getTeacherNo, scope.userNo());
        }

        List<CourseArrangement> arrangements = courseArrangementMapper.selectList(caQuery);

        List<CourseDifficultyDTO> results = new ArrayList<>();
        for (CourseArrangement ca : arrangements) {
            // Get scores for this course
            var scoreQuery = scoreService.lambdaQuery();
            scoreQuery.eq(Score::getCourseArrangementId, ca.getId());
            List<Score> scores = scoreQuery.list();

            if (scores.isEmpty()) continue;

            List<Double> scoreValues = scores.stream()
                    .map(Score::getTotalScore)
                    .filter(s -> s != null)
                    .map(BigDecimal::doubleValue)
                    .toList();

            if (scoreValues.isEmpty()) continue;

            double avg = scoreValues.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double stdDev = calculateStandardDeviation(scoreValues, avg);
            long passCount = scoreValues.stream().filter(s -> s >= 60).count();

            // Difficulty index = 1 - (average / 100)
            double difficultyIndex = 1 - (avg / 100);

            CourseDifficultyDTO dto = new CourseDifficultyDTO();
            dto.setCourseCode(ca.getCourseCode());
            dto.setCourseName(ca.getCourseName());
            dto.setSemester(ca.getSemester());
            dto.setDifficultyIndex(round2(difficultyIndex));
            dto.setDifficultyLevel(CourseDifficultyDTO.calculateDifficultyLevel(difficultyIndex));
            dto.setTotalStudents(scoreValues.size());
            dto.setAverageScore(round2(avg));
            dto.setPassRate(round2(100.0 * passCount / scoreValues.size()));

            // Discrimination index (simplified)
            dto.setDiscriminationIndex(round2(stdDev / 25));
            dto.setReliabilityIndex(round2(Math.min(1.0, stdDev / 15)));

            results.add(dto);
        }

        // Sort by difficulty
        results.sort((a, b) -> Double.compare(b.getDifficultyIndex(), a.getDifficultyIndex()));
        return results;
    }

    @Override
    public List<Map<String, Object>> getScoreRank(String semester, String classCode, Long courseArrangementId, Integer topN, Authentication authentication) {
        UserScope scope = resolveUserScope(authentication);

        var query = scoreService.lambdaQuery();
        query.select(Score::getStudentId, Score::getTotalScore);
        query.join(Score.class, Score::getCourseArrangementId, CourseArrangement.class, Score::getCourseArrangementId);
        query.leftJoin(Student.class, Score::getStudentId, Student::getStudentNo);

        if (StringUtils.hasText(semester)) {
            query.eq(CourseArrangement::getSemester, semester);
        }
        if (classCode != null) {
            query.eq(Student::getClassCode, classCode);
        }
        if (courseArrangementId != null) {
            query.eq(Score::getCourseArrangementId, courseArrangementId);
        }

        if (scope.role().isStudent()) {
            query.eq(Score::getStudentId, scope.userNo());
        }

        List<Score> scores = query.list();

        // Group and calculate average
        Map<String, List<Score>> byStudent = scores.stream()
                .filter(s -> s.getStudentId() != null)
                .collect(java.util.stream.Collectors.groupingBy(Score::getStudentId));

        List<Map<String, Object>> rankings = new ArrayList<>();
        int rank = 1;
        for (Map.Entry<String, List<Score>> entry : byStudent.entrySet()) {
            Student student = studentMapper.selectByStudentNo(entry.getKey());
            if (student == null) continue;

            List<Double> scoreValues = entry.getValue().stream()
                    .map(Score::getTotalScore)
                    .filter(s -> s != null)
                    .map(BigDecimal::doubleValue)
                    .toList();

            if (scoreValues.isEmpty()) continue;

            double avg = scoreValues.stream().mapToDouble(Double::doubleValue).average().orElse(0);

            Map<String, Object> row = new HashMap<>();
            row.put("rank", rank++);
            row.put("studentNo", student.getStudentNo());
            row.put("studentName", student.getName());
            row.put("classCode", student.getClassCode());
            row.put("averageScore", round2(avg));
            row.put("courseCount", scoreValues.size());
            rankings.add(row);
        }

        // Sort by average and take top N
        rankings.sort((a, b) -> Double.compare((Double) b.get("averageScore"), (Double) a.get("averageScore")));
        return rankings.stream().limit(topN).toList();
    }

    private double calculateStandardDeviation(List<Double> values, double mean) {
        if (values.isEmpty()) return 0;
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0);
        return Math.sqrt(variance);
    }

    private List<ScoreDistributionDTO.ScoreRange> calculateDistribution(List<Double> scores) {
        String[] ranges = {"0-59", "60-69", "70-79", "80-89", "90-100"};
        int[] bounds = {0, 60, 70, 80, 90, 101};

        List<ScoreDistributionDTO.ScoreRange> result = new ArrayList<>();
        for (int i = 0; i < ranges.length; i++) {
            int finalI = i;
            long count = scores.stream()
                    .filter(s -> s >= bounds[finalI] && s < bounds[finalI + 1])
                    .count();

            ScoreDistributionDTO.ScoreRange range = new ScoreDistributionDTO.ScoreRange();
            range.setRange(ranges[i]);
            range.setCount((int) count);
            range.setPercentage(scores.isEmpty() ? 0.0 : round2(100.0 * count / scores.size()));
            result.add(range);
        }
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
        if (scope.role().isStudent()) {
            return 1L;
        }
        if (arrangementScope.enabled()) {
            if (arrangementScope.arrangementIds().isEmpty()) {
                return 0L;
            }
            List<CourseArrangement> arrangements = courseArrangementMapper.selectList(
                    new LambdaQueryWrapper<CourseArrangement>().in(CourseArrangement::getId, arrangementScope.arrangementIds()));
            Set<String> classIds = new HashSet<>();
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
        if (scope.role().isStudent()) {
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
        if (!scope.role().isStudent()) {
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
        Set<String> courseCodes = new HashSet<>();
        for (CourseArrangement arrangement : arrangements) {
            if (arrangement.getCourseCode() != null) {
                courseCodes.add(arrangement.getCourseCode());
            }
        }
        return (long) courseCodes.size();
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

        Map<String, Aggregate> aggregateMap = new HashMap<>();
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

        Map<String, Aggregate> aggregateMap = new HashMap<>();
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

        Map<String, Aggregate> aggregateMap = new HashMap<>();
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

    private List<RiskStudentDTO> collectStudentLowScoreRiskDetails(DateRange range, UserScope scope, ArrangementScope arrangementScope) {
        if (arrangementScope.enabled() && arrangementScope.arrangementIds().isEmpty()) {
            return List.of();
        }
        if (scope.studentNo() == null) {
            return List.of();
        }

        var query = scoreService.lambdaQuery()
                .lt(Score::getTotalScore, PASS_SCORE)
                .between(Score::getCreateTime, range.startDate().atStartOfDay(), range.endDate().plusDays(1).atStartOfDay().minusNanos(1));
        applyScoreUserScope(query, scope);
        applyArrangementScope(query, arrangementScope, Score::getCourseArrangementId);
        List<Score> records = query.list();
        if (records.isEmpty()) {
            return List.of();
        }

        Set<Long> arrangementIds = new HashSet<>();
        for (Score record : records) {
            if (record.getCourseArrangementId() != null) {
                arrangementIds.add(record.getCourseArrangementId());
            }
        }
        Map<Long, CourseRef> courseRefByArrangementId = resolveCourseRefByArrangementIds(arrangementIds);
        Student student = studentMapper.selectByStudentNoWithClass(scope.studentNo());

        Map<String, LowScoreDetailAggregate> aggregateByCourse = new HashMap<>();
        for (Score score : records) {
            if (score.getTotalScore() == null) {
                continue;
            }
            Long arrangementId = score.getCourseArrangementId();
            CourseRef courseRef = arrangementId == null ? null : courseRefByArrangementId.get(arrangementId);
            String courseCode = courseRef == null ? null : courseRef.courseCode();
            String groupKey = courseCode != null ? "course_" + courseCode : "arrangement_" + arrangementId;
            String courseName = courseRef == null || !StringUtils.hasText(courseRef.courseName()) ? "-" : courseRef.courseName();
            double scoreValue = score.getTotalScore().doubleValue();
            aggregateByCourse.computeIfAbsent(groupKey, key -> new LowScoreDetailAggregate(courseName)).add(scoreValue);
        }

        List<RiskStudentDTO> details = new ArrayList<>();
        for (LowScoreDetailAggregate aggregate : aggregateByCourse.values()) {
            RiskStudentDTO dto = new RiskStudentDTO();
            fillStudentBase(dto, student);
            dto.setRiskType("low_score");
            dto.setCourseName(aggregate.courseName());
            dto.setRiskCount(aggregate.count());
            dto.setScore(round2(aggregate.minScore()));
            dto.setRiskValue(round2(aggregate.minScore()));
            details.add(dto);
        }
        return details;
    }

    private List<RiskStudentDTO> collectStudentAbnormalAttendanceRiskDetails(DateRange range, UserScope scope, ArrangementScope arrangementScope) {
        if (arrangementScope.enabled() && arrangementScope.arrangementIds().isEmpty()) {
            return List.of();
        }
        if (scope.studentNo() == null) {
            return List.of();
        }

        var query = attendanceService.lambdaQuery()
                .between(Attendance::getAttendanceDate, range.startDate(), range.endDate())
                .in(Attendance::getStatus, Attendance.Status.ABSENT, Attendance.Status.LATE);
        applyAttendanceUserScope(query, scope);
        applyArrangementScope(query, arrangementScope, Attendance::getCourseArrangementId);
        List<Attendance> records = query.list();
        if (records.isEmpty()) {
            return List.of();
        }

        Set<Long> arrangementIds = new HashSet<>();
        for (Attendance record : records) {
            if (record.getCourseArrangementId() != null) {
                arrangementIds.add(record.getCourseArrangementId());
            }
        }
        Map<Long, CourseRef> courseRefByArrangementId = resolveCourseRefByArrangementIds(arrangementIds);
        Student student = studentMapper.selectByStudentNoWithClass(scope.studentNo());

        List<RiskStudentDTO> details = new ArrayList<>();
        for (Attendance attendance : records) {
            RiskStudentDTO dto = new RiskStudentDTO();
            fillStudentBase(dto, student);
            dto.setRiskType("abnormal_attendance");
            dto.setRiskCount(1L);
            dto.setRiskValue(1D);
            dto.setAttendanceDate(attendance.getAttendanceDate());
            dto.setAttendanceStatus(attendance.getStatus() == null ? null : attendance.getStatus().name());
            CourseRef courseRef = attendance.getCourseArrangementId() == null ? null : courseRefByArrangementId.get(attendance.getCourseArrangementId());
            dto.setCourseName(courseRef == null || !StringUtils.hasText(courseRef.courseName()) ? "-" : courseRef.courseName());
            details.add(dto);
        }
        return details;
    }

    private List<RiskStudentDTO> collectStudentApprovalOverdueRiskDetails(UserScope scope, ArrangementScope arrangementScope) {
        if (arrangementScope.enabled() && arrangementScope.arrangementIds().isEmpty()) {
            return List.of();
        }
        if (scope.studentNo() == null) {
            return List.of();
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusHours(OVERDUE_THRESHOLD_HOURS);
        var query = leaveRequestService.lambdaQuery()
                .eq(LeaveRequest::getStatus, LeaveRequest.Status.PENDING)
                .le(LeaveRequest::getCreateTime, threshold);
        applyLeaveUserScope(query, scope);
        applyArrangementScope(query, arrangementScope, LeaveRequest::getCourseArrangementId);
        List<LeaveRequest> records = query.list();
        if (records.isEmpty()) {
            return List.of();
        }

        Set<Long> arrangementIds = new HashSet<>();
        for (LeaveRequest record : records) {
            if (record.getCourseArrangementId() != null) {
                arrangementIds.add(record.getCourseArrangementId());
            }
        }
        Map<Long, CourseRef> courseRefByArrangementId = resolveCourseRefByArrangementIds(arrangementIds);
        Student student = studentMapper.selectByStudentNoWithClass(scope.studentNo());

        List<RiskStudentDTO> details = new ArrayList<>();
        for (LeaveRequest leaveRequest : records) {
            if (leaveRequest.getCreateTime() == null) {
                continue;
            }
            double overdueHours = Duration.between(leaveRequest.getCreateTime(), now).toHours();
            RiskStudentDTO dto = new RiskStudentDTO();
            fillStudentBase(dto, student);
            dto.setRiskType("approval_overdue");
            dto.setRiskCount(1L);
            dto.setRiskValue(round2(overdueHours));
            dto.setLeaveRequestId(leaveRequest.getId());
            dto.setSubmitTime(leaveRequest.getCreateTime());
            dto.setOverdue(true);
            dto.setOverdueHours(round2(overdueHours));
            CourseRef courseRef = leaveRequest.getCourseArrangementId() == null ? null : courseRefByArrangementId.get(leaveRequest.getCourseArrangementId());
            dto.setCourseName(courseRef == null || !StringUtils.hasText(courseRef.courseName()) ? "-" : courseRef.courseName());
            details.add(dto);
        }
        return details;
    }

    private List<RiskStudentDTO> toRiskStudentList(Map<String, Aggregate> aggregateMap, String riskType, boolean averageValue) {
        if (aggregateMap.isEmpty()) {
            return List.of();
        }
        List<String> studentIds = new ArrayList<>(aggregateMap.keySet());
        List<Student> students = studentMapper.selectByStudentNosWithClass(studentIds);
        Map<String, Student> studentById = new HashMap<>();
        for (Student student : students) {
            if (student.getStudentNo() != null) {
                studentById.put(student.getStudentNo(), student);
            }
        }

        List<RiskStudentDTO> list = new ArrayList<>();
        for (Map.Entry<String, Aggregate> entry : aggregateMap.entrySet()) {
            Student student = studentById.get(entry.getKey());
            if (student == null) {
                continue;
            }
            Aggregate aggregate = entry.getValue();
            RiskStudentDTO dto = new RiskStudentDTO();
            dto.setStudentId(student.getStudentNo());
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

    private Comparator<RiskStudentDTO> buildStudentRiskComparator(String riskType) {
        return switch (riskType) {
            case "low_score" -> Comparator
                    .comparing(RiskStudentDTO::getScore, Comparator.nullsLast(Double::compareTo))
                    .thenComparing(RiskStudentDTO::getRiskCount, Comparator.nullsLast(Comparator.reverseOrder()));
            case "abnormal_attendance" -> Comparator
                    .comparing(RiskStudentDTO::getAttendanceDate, Comparator.nullsLast(Comparator.reverseOrder()));
            case "approval_overdue" -> Comparator
                    .comparing(RiskStudentDTO::getOverdueHours, Comparator.nullsLast(Comparator.reverseOrder()));
            default -> Comparator.comparing(RiskStudentDTO::getRiskValue, Comparator.nullsLast(Double::compareTo)).reversed();
        };
    }

    private Map<Long, CourseRef> resolveCourseRefByArrangementIds(Set<Long> arrangementIds) {
        if (arrangementIds == null || arrangementIds.isEmpty()) {
            return Map.of();
        }

        List<CourseArrangement> arrangements = courseArrangementMapper.selectList(
                new LambdaQueryWrapper<CourseArrangement>().in(CourseArrangement::getId, arrangementIds));
        if (arrangements.isEmpty()) {
            return Map.of();
        }

        Set<String> courseCodes = new HashSet<>();
        for (CourseArrangement arrangement : arrangements) {
            if (arrangement.getCourseCode() != null) {
                courseCodes.add(arrangement.getCourseCode());
            }
        }

        Map<Long, String> courseNameById = new HashMap<>();
        if (!courseCodes.isEmpty()) {
            List<Course> courses = courseMapper.selectBatchIds(courseCodes);
            for (Course course : courses) {
                if (course.getId() != null) {
                    courseNameById.put(course.getId(), course.getCourseName());
                }
            }
        }

        Map<Long, CourseRef> result = new HashMap<>();
        for (CourseArrangement arrangement : arrangements) {
            if (arrangement.getId() == null) {
                continue;
            }
            String courseName = arrangement.getCourseCode() == null ? null : courseNameById.get(arrangement.getCourseCode());
            result.put(arrangement.getId(), new CourseRef(arrangement.getCourseCode(), courseName));
        }
        return result;
    }

    private void fillStudentBase(RiskStudentDTO dto, Student student) {
        if (student == null) {
            return;
        }
        dto.setStudentId(student.getStudentNo());
        dto.setStudentNo(student.getStudentNo());
        dto.setStudentName(student.getName());
        dto.setClassName(student.getClassName());
    }

    private UserScope resolveUserScope(Authentication authentication) {
        RoleCode primaryRoleCode = currentUserService.getPrimaryRoleCode(authentication);
        SysUser.Role primaryRole = RoleCode.toUserRole(primaryRoleCode);
        if (primaryRole == null) {
            throw new BusinessException(403, "No valid role found");
        }
        if (primaryRole.isStudent()) {
            Student student = currentUserService.getCurrentStudent(authentication);
            return new UserScope(primaryRole, student.getStudentNo(), null, student.getClassId(), null);
        }
        if (primaryRole.isTeacherGroup()) {
            String teacherNo = currentUserService.getCurrentTeacherNo(authentication);
            return new UserScope(primaryRole, null, teacherNo, null, null);
        }
        String managedCollegeCode = null;
        if (primaryRole == SysUser.Role.COLLEGE_ADMIN) {
            managedCollegeCode = currentUserService.resolveManagedCollegeCode(authentication);
        }
        return new UserScope(primaryRole, null, null, null, managedCollegeCode);
    }

    private ArrangementScope resolveArrangementScope(AnalyticsFilterDTO filter, UserScope scope) {
        String teacherNo = null;
        String classId = null;
        if (scope.role().isTeacherGroup()) {
            teacherNo = scope.teacherNo();
            classId = filter.getClassId();
        } else if (scope.role().isAdminGroup()) {
            teacherNo = filter.getTeacherNo();
            classId = filter.getClassId();
        }
        String semester = filter.getSemester();

        // Student analytics must stay in "self" scope; classId/teacherNo filters are ignored.
        // Optional semester filter is still supported for narrowing personal records.
        boolean enabled;
        if (scope.role().isAdminGroup()) {
            enabled = teacherNo != null || classId != null || StringUtils.hasText(semester);
            if (scope.collegeCode() != null) {
                enabled = true;
            }
        } else if (scope.role().isTeacherGroup()) {
            enabled = true;
        } else {
            enabled = StringUtils.hasText(semester);
        }
        if (!enabled) {
            return new ArrangementScope(false, Set.of());
        }

        var query = new LambdaQueryWrapper<CourseArrangement>();
        if (teacherNo != null) {
            query.eq(CourseArrangement::getTeacherNo, teacherNo);
        }
        if (classId != null) {
            query.eq(CourseArrangement::getClassId, classId);
        }
        if (scope.collegeCode() != null) {
            List<Class> classes = classMapper.selectList(
                    new LambdaQueryWrapper<Class>().eq(Class::getCollegeCode, scope.collegeCode()));
            Set<String> classIds = classes.stream()
                    .map(Class::getClassCode)
                    .filter(id -> id != null)
                    .collect(java.util.stream.Collectors.toSet());
            if (classIds.isEmpty()) {
                return new ArrangementScope(true, Set.of());
            }
            query.in(CourseArrangement::getClassId, classIds);
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
        if (scope.role().isStudent() && scope.studentNo() != null) {
            query.eq(Score::getStudentId, scope.studentNo());
        }
    }

    private void applyAttendanceUserScope(com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper<Attendance> query, UserScope scope) {
        if (scope.role().isStudent() && scope.studentNo() != null) {
            query.eq(Attendance::getStudentId, scope.studentNo());
        }
    }

    private void applyLeaveUserScope(com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper<LeaveRequest> query, UserScope scope) {
        if (scope.role().isStudent() && scope.studentNo() != null) {
            query.eq(LeaveRequest::getStudentId, scope.studentNo());
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

    private record UserScope(SysUser.Role role, String studentNo, String teacherNo, String classId, String collegeCode) {
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

    private record CourseRef(String courseCode, String courseName) {
    }

    private static class LowScoreDetailAggregate {
        private final String courseName;
        private long count = 0;
        private double minScore = Double.MAX_VALUE;

        private LowScoreDetailAggregate(String courseName) {
            this.courseName = courseName;
        }

        private void add(double score) {
            count++;
            if (score < minScore) {
                minScore = score;
            }
        }

        private String courseName() {
            return courseName;
        }

        private long count() {
            return count;
        }

        private double minScore() {
            return minScore == Double.MAX_VALUE ? 0D : minScore;
        }
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


