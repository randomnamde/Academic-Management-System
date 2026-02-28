package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.ScoreQueryDTO;
import com.student.entity.Attendance;
import com.student.entity.LeaveRequest;
import com.student.entity.Score;
import com.student.security.DataScopeService;
import com.student.service.AttendanceService;
import com.student.service.LeaveRequestService;
import com.student.service.ScoreService;
import com.student.util.CsvExportUtil;
import com.student.util.ExcelExportUtil;
import com.student.util.ExportFormat;
import com.student.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private static final int EXPORT_LIMIT = 10000;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ScoreService scoreService;
    private final AttendanceService attendanceService;
    private final LeaveRequestService leaveRequestService;
    private final DataScopeService dataScopeService;

    @GetMapping("/score")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public void exportScore(@RequestParam(required = false) Long studentId,
                            @RequestParam(required = false) Long courseArrangementId,
                            @RequestParam(required = false) String semester,
                            @RequestParam(required = false) String format,
                            Authentication authentication,
                            HttpServletResponse response) {
        Long scopedStudentId = dataScopeService.resolveScopedStudentId(authentication, studentId);
        Long scopedTeacherId = dataScopeService.resolveScopedTeacherId(authentication, null);
        dataScopeService.assertTeacherOwnsArrangement(authentication, courseArrangementId);
        ScoreQueryDTO queryDTO = new ScoreQueryDTO();
        queryDTO.setStudentId(scopedStudentId);
        queryDTO.setTeacherId(scopedTeacherId);
        queryDTO.setCourseArrangementId(courseArrangementId);
        queryDTO.setSemester(semester);
        Page<Score> result = scoreService.getScorePage(1, EXPORT_LIMIT, queryDTO);

        List<String> headers = List.of("学号", "姓名", "课程", "学期", "平时分", "期中分", "期末分", "总评", "GPA", "状态", "考试时间");
        List<List<String>> rows = new ArrayList<>();
        for (Score score : result.getRecords()) {
            rows.add(List.of(
                    safe(score.getStudentNo()),
                    safe(score.getStudentName()),
                    safe(score.getCourseName()),
                    safe(score.getSemester()),
                    safe(score.getUsualScore()),
                    safe(score.getMidtermScore()),
                    safe(score.getFinalScore()),
                    safe(score.getTotalScore()),
                    safe(score.getGpa()),
                    safe(score.getStatus()),
                    formatDateTime(score.getExamTime())
            ));
        }

        writeExport(response, ExportFormat.fromNullable(format), "成绩单", "score", headers, rows);
    }

    @GetMapping("/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public void exportAttendance(@RequestParam(required = false) Long studentId,
                                 @RequestParam(required = false) Long courseArrangementId,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate attendanceDate,
                                 @RequestParam(required = false) Attendance.Status status,
                                 @RequestParam(required = false) String format,
                                 Authentication authentication,
                                 HttpServletResponse response) {
        Long scopedStudentId = dataScopeService.resolveScopedStudentId(authentication, studentId);
        Long scopedTeacherId = dataScopeService.resolveScopedTeacherId(authentication, null);
        dataScopeService.assertTeacherOwnsArrangement(authentication, courseArrangementId);
        Page<Attendance> result = attendanceService.getAttendancePage(
                1, EXPORT_LIMIT, scopedStudentId, scopedTeacherId, courseArrangementId, attendanceDate, status);

        List<String> headers = List.of("学号", "姓名", "班级", "课程", "考勤日期", "状态", "签到时间", "签退时间", "备注");
        List<List<String>> rows = new ArrayList<>();
        for (Attendance attendance : result.getRecords()) {
            rows.add(List.of(
                    safe(attendance.getStudentNo()),
                    safe(attendance.getStudentName()),
                    safe(attendance.getClassName()),
                    safe(attendance.getCourseName()),
                    safe(attendance.getAttendanceDate()),
                    safe(attendance.getStatus()),
                    safe(attendance.getCheckInTime()),
                    safe(attendance.getCheckOutTime()),
                    safe(attendance.getRemark())
            ));
        }
        writeExport(response, ExportFormat.fromNullable(format), "考勤明细", "attendance", headers, rows);
    }

    @GetMapping("/leave-request")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public void exportLeaveRequest(@RequestParam(required = false) Long studentId,
                                   @RequestParam(required = false) LeaveRequest.Status status,
                                   @RequestParam(required = false) String format,
                                   Authentication authentication,
                                   HttpServletResponse response) {
        Long scopedStudentId = dataScopeService.resolveScopedStudentId(authentication, studentId);
        Long scopedTeacherId = dataScopeService.resolveScopedTeacherId(authentication, null);
        Page<LeaveRequest> result = leaveRequestService.getLeaveRequestPage(
                1, EXPORT_LIMIT, scopedStudentId, scopedTeacherId, status);

        List<String> headers = List.of("学号", "姓名", "班级", "课程", "请假类型", "开始时间", "结束时间", "状态", "审批人", "审批备注", "提交时间");
        List<List<String>> rows = new ArrayList<>();
        for (LeaveRequest request : result.getRecords()) {
            rows.add(List.of(
                    safe(request.getStudentNo()),
                    safe(request.getStudentName()),
                    safe(request.getClassName()),
                    safe(request.getCourseName()),
                    safe(request.getLeaveType()),
                    formatDateTime(request.getStartTime()),
                    formatDateTime(request.getEndTime()),
                    safe(request.getStatus()),
                    safe(request.getApproverName()),
                    safe(request.getApproveRemark()),
                    formatDateTime(request.getCreateTime())
            ));
        }
        writeExport(response, ExportFormat.fromNullable(format), "请假统计", "leave-request", headers, rows);
    }

    @GetMapping("/formats")
    public ResultVO<List<String>> supportedFormats() {
        return ResultVO.success(List.of("csv", "xlsx"));
    }

    private void writeExport(HttpServletResponse response,
                             ExportFormat format,
                             String sheetName,
                             String filePrefix,
                             List<String> headers,
                             List<List<String>> rows) {
        try {
            byte[] bytes;
            String ext;
            if (format == ExportFormat.XLSX) {
                bytes = ExcelExportUtil.write(sheetName, headers, rows);
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                ext = "xlsx";
            } else {
                bytes = CsvExportUtil.write(headers, rows);
                response.setContentType("text/csv;charset=UTF-8");
                ext = "csv";
            }

            String fileName = filePrefix + "-" + LocalDate.now() + "." + ext;
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName);
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to export report", e);
        }
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String formatDateTime(java.time.LocalDateTime value) {
        return value == null ? "" : value.format(DATETIME_FORMATTER);
    }
}
