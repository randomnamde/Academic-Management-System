package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.AttendanceDTO;
import com.student.entity.Attendance;
import com.student.service.AttendanceService;
import com.student.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<Void> record(@RequestBody @Validated AttendanceDTO attendanceDTO) {
        attendanceService.recordAttendance(attendanceDTO);
        return ResultVO.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<Void> update(@PathVariable Long id, @RequestBody @Validated AttendanceDTO attendanceDTO) {
        attendanceDTO.setId(id);
        attendanceService.updateAttendance(attendanceDTO);
        return ResultVO.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<Void> delete(@PathVariable Long id) {
        attendanceService.deleteAttendance(id);
        return ResultVO.success();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<Attendance> getById(@PathVariable Long id) {
        Attendance attendance = attendanceService.getAttendanceById(id);
        return ResultVO.success(attendance);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<Page<Attendance>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseArrangementId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate attendanceDate,
            @RequestParam(required = false) Attendance.Status status) {
        Page<Attendance> result = attendanceService.getAttendancePage(page, size, studentId, 
                                                                        courseArrangementId, attendanceDate, status);
        return ResultVO.success(result);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<List<Attendance>> getByStudentId(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Attendance> attendances = attendanceService.getStudentAttendance(studentId, startDate, endDate);
        return ResultVO.success(attendances);
    }

    @GetMapping("/statistics/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<Map<String, Object>> getStatistics(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> statistics = attendanceService.getAttendanceStatistics(studentId, startDate, endDate);
        return ResultVO.success(statistics);
    }

    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<Void> batchRecord(@RequestBody List<AttendanceDTO> attendanceDTOList) {
        attendanceService.batchRecordAttendance(attendanceDTOList);
        return ResultVO.success();
    }

    @PostMapping("/check-in")
    @PreAuthorize("hasRole('STUDENT')")
    public ResultVO<Void> checkIn(@RequestParam Long courseArrangementId, 
                                     @RequestAttribute("userId") Long studentId) {
        attendanceService.checkIn(studentId, courseArrangementId);
        return ResultVO.success();
    }

    @PostMapping("/check-out")
    @PreAuthorize("hasRole('STUDENT')")
    public ResultVO<Void> checkOut(@RequestParam Long courseArrangementId,
                                      @RequestAttribute("userId") Long studentId) {
        attendanceService.checkOut(studentId, courseArrangementId);
        return ResultVO.success();
    }
}
