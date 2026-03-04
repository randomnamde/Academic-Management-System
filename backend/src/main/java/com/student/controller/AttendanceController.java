package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.AttendanceDTO;
import com.student.entity.Attendance;
import com.student.entity.CourseArrangement;
import com.student.mapper.CourseArrangementMapper;
import com.student.security.CurrentUserService;
import com.student.security.DataScopeService;
import com.student.service.AttendanceService;
import com.student.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final CurrentUserService currentUserService;
    private final DataScopeService dataScopeService;
    private final CourseArrangementMapper courseArrangementMapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<Void> record(@RequestBody @Validated AttendanceDTO attendanceDTO, Authentication authentication) {
        assertCollegeAdminArrangementScope(authentication, attendanceDTO.getCourseArrangementId());
        dataScopeService.assertTeacherOwnsArrangement(authentication, attendanceDTO.getCourseArrangementId());
        attendanceService.recordAttendance(attendanceDTO);
        return ResultVO.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<Void> update(@PathVariable Long id,
                                 @RequestBody @Validated AttendanceDTO attendanceDTO,
                                 Authentication authentication) {
        Attendance existing = attendanceService.getAttendanceById(id);
        if (existing == null) {
            return ResultVO.error(404, "Attendance record not found");
        }
        assertCollegeAdminArrangementScope(authentication, existing.getCourseArrangementId());
        assertCollegeAdminArrangementScope(authentication, attendanceDTO.getCourseArrangementId());
        dataScopeService.assertTeacherOwnsArrangement(authentication, existing.getCourseArrangementId());
        dataScopeService.assertTeacherOwnsArrangement(authentication, attendanceDTO.getCourseArrangementId());
        attendanceDTO.setId(id);
        attendanceService.updateAttendance(attendanceDTO);
        return ResultVO.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<Void> delete(@PathVariable Long id, Authentication authentication) {
        Attendance existing = attendanceService.getAttendanceById(id);
        if (existing == null) {
            return ResultVO.error(404, "Attendance record not found");
        }
        assertCollegeAdminArrangementScope(authentication, existing.getCourseArrangementId());
        dataScopeService.assertTeacherOwnsArrangement(authentication, existing.getCourseArrangementId());
        attendanceService.deleteAttendance(id);
        return ResultVO.success();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<Attendance> getById(@PathVariable Long id, Authentication authentication) {
        Attendance attendance = attendanceService.getAttendanceById(id);
        if (attendance == null) {
            return ResultVO.error(404, "Attendance record not found");
        }
        assertCollegeAdminArrangementScope(authentication, attendance.getCourseArrangementId());
        if (currentUserService.isStudent(authentication)) {
            Long studentId = currentUserService.getCurrentStudentId(authentication);
            if (!studentId.equals(attendance.getStudentId())) {
                return ResultVO.error(403, "Forbidden");
            }
        }
        dataScopeService.assertTeacherOwnsArrangement(authentication, attendance.getCourseArrangementId());
        return ResultVO.success(attendance);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<Page<Attendance>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseArrangementId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate attendanceDate,
            @RequestParam(required = false) Attendance.Status status,
            Authentication authentication) {
        assertCollegeAdminArrangementScope(authentication, courseArrangementId);
        Long scopedStudentId = dataScopeService.resolveScopedStudentId(authentication, studentId);
        Long scopedTeacherId = dataScopeService.resolveScopedTeacherId(authentication, null);
        dataScopeService.assertTeacherOwnsArrangement(authentication, courseArrangementId);
        Page<Attendance> result = attendanceService.getAttendancePage(
                page, size, scopedStudentId, scopedTeacherId, courseArrangementId, attendanceDate, status);
        return ResultVO.success(result);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<List<Attendance>> getByStudentId(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        Long scopedStudentId = dataScopeService.resolveScopedStudentId(authentication, studentId);
        Long scopedTeacherId = dataScopeService.resolveScopedTeacherId(authentication, null);
        if (scopedTeacherId != null) {
            Page<Attendance> result = attendanceService.getAttendancePage(
                    1, 10000, scopedStudentId, scopedTeacherId, null, null, null);
            List<Attendance> records = filterByDateRange(result.getRecords(), startDate, endDate);
            return ResultVO.success(records);
        }
        List<Attendance> attendances = attendanceService.getStudentAttendance(scopedStudentId, startDate, endDate);
        return ResultVO.success(attendances);
    }

    @GetMapping("/statistics/{studentId}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<Map<String, Object>> getStatistics(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        Long scopedStudentId = dataScopeService.resolveScopedStudentId(authentication, studentId);
        Long scopedTeacherId = dataScopeService.resolveScopedTeacherId(authentication, null);
        if (scopedTeacherId != null) {
            Page<Attendance> result = attendanceService.getAttendancePage(
                    1, 10000, scopedStudentId, scopedTeacherId, null, null, null);
            List<Attendance> records = filterByDateRange(result.getRecords(), startDate, endDate);
            return ResultVO.success(buildStatistics(records));
        }
        Map<String, Object> statistics = attendanceService.getAttendanceStatistics(scopedStudentId, startDate, endDate);
        return ResultVO.success(statistics);
    }

    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<Void> batchRecord(@RequestBody List<AttendanceDTO> attendanceDTOList, Authentication authentication) {
        for (AttendanceDTO item : attendanceDTOList) {
            assertCollegeAdminArrangementScope(authentication, item.getCourseArrangementId());
        }
        dataScopeService.assertTeacherOwnsArrangements(
                authentication,
                attendanceDTOList.stream().map(AttendanceDTO::getCourseArrangementId).toList());
        attendanceService.batchRecordAttendance(attendanceDTOList);
        return ResultVO.success();
    }

    private void assertCollegeAdminArrangementScope(Authentication authentication, Long arrangementId) {
        Long scopedCollegeId = dataScopeService.resolveScopedCollegeId(authentication);
        if (scopedCollegeId == null || arrangementId == null) {
            return;
        }
        CourseArrangement arrangement = courseArrangementMapper.selectById(arrangementId);
        if (arrangement == null) {
            return;
        }
        java.util.Set<Long> classIds = dataScopeService.resolveCollegeClassIds(authentication);
        if (!classIds.contains(arrangement.getClassId())) {
            throw new com.student.exception.BusinessException(403, "Forbidden");
        }
    }

    @PostMapping("/check-in")
    @PreAuthorize("hasRole('STUDENT')")
    public ResultVO<Void> checkIn(@RequestParam Long courseArrangementId, 
                                  Authentication authentication) {
        Long studentId = currentUserService.getCurrentStudentId(authentication);
        attendanceService.checkIn(studentId, courseArrangementId);
        return ResultVO.success();
    }

    @PostMapping("/check-out")
    @PreAuthorize("hasRole('STUDENT')")
    public ResultVO<Void> checkOut(@RequestParam Long courseArrangementId,
                                   Authentication authentication) {
        Long studentId = currentUserService.getCurrentStudentId(authentication);
        attendanceService.checkOut(studentId, courseArrangementId);
        return ResultVO.success();
    }

    private List<Attendance> filterByDateRange(List<Attendance> source, LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate == null ? LocalDate.of(1970, 1, 1) : startDate;
        LocalDate end = endDate == null ? LocalDate.of(2999, 12, 31) : endDate;
        return source.stream()
                .filter(item -> item.getAttendanceDate() != null)
                .filter(item -> !item.getAttendanceDate().isBefore(start) && !item.getAttendanceDate().isAfter(end))
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildStatistics(List<Attendance> records) {
        long presentCount = records.stream().filter(item -> item.getStatus() == Attendance.Status.PRESENT).count();
        long absentCount = records.stream().filter(item -> item.getStatus() == Attendance.Status.ABSENT).count();
        long lateCount = records.stream().filter(item -> item.getStatus() == Attendance.Status.LATE).count();
        long leaveCount = records.stream().filter(item -> item.getStatus() == Attendance.Status.LEAVE).count();
        long total = records.size();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("presentCount", presentCount);
        statistics.put("absentCount", absentCount);
        statistics.put("lateCount", lateCount);
        statistics.put("leaveCount", leaveCount);
        statistics.put("totalCount", total);
        if (total == 0) {
            statistics.put("presentRate", 0.0);
            statistics.put("absentRate", 0.0);
            statistics.put("lateRate", 0.0);
            statistics.put("leaveRate", 0.0);
            return statistics;
        }

        statistics.put("presentRate", (double) presentCount / total * 100);
        statistics.put("absentRate", (double) absentCount / total * 100);
        statistics.put("lateRate", (double) lateCount / total * 100);
        statistics.put("leaveRate", (double) leaveCount / total * 100);
        return statistics;
    }
}

