package com.student.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.dto.DashboardOverviewDTO;
import com.student.entity.Attendance;
import com.student.entity.CourseArrangement;
import com.student.entity.LeaveRequest;
import com.student.entity.Score;
import com.student.entity.SysUser;
import com.student.mapper.CourseArrangementMapper;
import com.student.security.CurrentUserService;
import com.student.service.AttendanceService;
import com.student.service.LeaveRequestService;
import com.student.service.ScoreService;
import com.student.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private static final BigDecimal LOW_SCORE_THRESHOLD = new BigDecimal("60");

    private final CurrentUserService currentUserService;
    private final AttendanceService attendanceService;
    private final LeaveRequestService leaveRequestService;
    private final ScoreService scoreService;
    private final CourseArrangementMapper courseArrangementMapper;

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<DashboardOverviewDTO> getOverview(Authentication authentication) {
        SysUser currentUser = currentUserService.getCurrentUser(authentication);
        String role = currentUser.getRole().name();

        Long studentId = null;
        Long teacherId = null;
        List<Long> teacherArrangementIds = new ArrayList<>();
        if (currentUserService.isStudent(authentication)) {
            studentId = currentUserService.getCurrentStudentId(authentication);
        } else if (currentUserService.isTeacher(authentication)) {
            teacherId = currentUserService.getCurrentTeacherId(authentication);
            teacherArrangementIds = courseArrangementMapper.selectList(
                            new LambdaQueryWrapper<CourseArrangement>().eq(CourseArrangement::getTeacherId, teacherId))
                    .stream()
                    .map(CourseArrangement::getId)
                    .collect(Collectors.toList());
        }

        DashboardOverviewDTO overview = new DashboardOverviewDTO();
        overview.setRole(role);
        overview.setPendingApprovalCount(countPending(role, studentId, teacherId));
        overview.setAbnormalTodayCount(countAbnormalByDate(LocalDate.now(), role, studentId, teacherArrangementIds));
        overview.setLowScoreWarningCount(countLowScore(role, studentId, teacherArrangementIds));
        overview.setAbnormalTrend(buildTrend(role, studentId, teacherArrangementIds));
        return ResultVO.success(overview);
    }

    private Long countPending(String role, Long studentId, Long teacherId) {
        if ("STUDENT".equals(role)) {
            return leaveRequestService.lambdaQuery()
                    .eq(LeaveRequest::getStudentId, studentId)
                    .eq(LeaveRequest::getStatus, LeaveRequest.Status.PENDING)
                    .count();
        }
        if ("TEACHER".equals(role)) {
            return (long) leaveRequestService.getPendingRequestsForTeacher(teacherId).size();
        }
        return leaveRequestService.lambdaQuery()
                .eq(LeaveRequest::getStatus, LeaveRequest.Status.PENDING)
                .count();
    }

    private Long countAbnormalByDate(LocalDate date,
                                     String role,
                                     Long studentId,
                                     List<Long> teacherArrangementIds) {
        var query = attendanceService.lambdaQuery()
                .eq(Attendance::getAttendanceDate, date)
                .in(Attendance::getStatus, Attendance.Status.ABSENT, Attendance.Status.LATE);
        if ("STUDENT".equals(role)) {
            query.eq(Attendance::getStudentId, studentId);
        } else if ("TEACHER".equals(role)) {
            if (teacherArrangementIds.isEmpty()) {
                return 0L;
            }
            query.in(Attendance::getCourseArrangementId, teacherArrangementIds);
        }
        return query.count();
    }

    private Long countLowScore(String role, Long studentId, List<Long> teacherArrangementIds) {
        var query = scoreService.lambdaQuery().lt(Score::getTotalScore, LOW_SCORE_THRESHOLD);
        if ("STUDENT".equals(role)) {
            query.eq(Score::getStudentId, studentId);
        } else if ("TEACHER".equals(role)) {
            if (teacherArrangementIds.isEmpty()) {
                return 0L;
            }
            query.in(Score::getCourseArrangementId, teacherArrangementIds);
        }
        return query.count();
    }

    private List<DashboardOverviewDTO.TrendPoint> buildTrend(String role,
                                                             Long studentId,
                                                             List<Long> teacherArrangementIds) {
        List<DashboardOverviewDTO.TrendPoint> trendPoints = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Long count = countAbnormalByDate(date, role, studentId, teacherArrangementIds);
            trendPoints.add(new DashboardOverviewDTO.TrendPoint(date, count));
        }
        return trendPoints;
    }
}
