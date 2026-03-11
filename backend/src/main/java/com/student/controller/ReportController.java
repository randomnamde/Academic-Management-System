package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.ScoreQueryDTO;
import com.student.entity.Attendance;
import com.student.entity.CourseArrangement;
import com.student.entity.LeaveRequest;
import com.student.entity.Score;
import com.student.security.DataScopeService;
import com.student.security.CurrentUserService;
import com.student.mapper.CourseArrangementMapper;
import com.student.service.AttendanceService;
import com.student.service.LeaveRequestService;
import com.student.service.ScoreService;
import com.student.util.CsvExportUtil;
import com.student.util.ExcelExportUtil;
import com.student.util.ExportFormat;
import com.student.vo.ResultVO;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    private final CurrentUserService currentUserService;
    private final CourseArrangementMapper courseArrangementMapper;

    @GetMapping("/score")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public void exportScore(@RequestParam(required = false) String studentId,
                            @RequestParam(required = false) Long courseArrangementId,
                            @RequestParam(required = false) Long collegeId,
                            @RequestParam(required = false) String classId,
                            @RequestParam(required = false) String semester,
                            @RequestParam(required = false) String format,
                            Authentication authentication,
                            HttpServletResponse response) {
        String scopedStudentId = dataScopeService.resolveScopedStudentNo(authentication, studentId);
        Long scopedTeacherId = dataScopeService.resolveScopedTeacherId(authentication, null);
        dataScopeService.assertTeacherOwnsArrangement(authentication, courseArrangementId);
        Set<Long> scopedArrangementIds = resolveCollegeArrangementScope(authentication, courseArrangementId);

        ScoreQueryDTO queryDTO = new ScoreQueryDTO();
        queryDTO.setStudentId(scopedStudentId);
        queryDTO.setTeacherId(scopedTeacherId);
        queryDTO.setCourseArrangementId(courseArrangementId);
        queryDTO.setCollegeId(collegeId);
        queryDTO.setClassId(classId);
        queryDTO.setSemester(semester);

        Page<Score> result = scoreService.getScorePage(1, EXPORT_LIMIT, queryDTO);
        List<Score> records = applyArrangementScope(result.getRecords(), scopedArrangementIds);

        List<String> headers = List.of(
                "\u5b66\u53f7",
                "\u59d3\u540d",
                "\u73ed\u7ea7",
                "\u8bfe\u7a0b",
                "\u5b66\u671f",
                "\u5e73\u65f6\u5206",
                "\u671f\u4e2d\u5206",
                "\u671f\u672b\u5206",
                "\u603b\u8bc4",
                "GPA",
                "\u72b6\u6001",
                "\u8003\u8bd5\u65f6\u95f4"
        );

        List<List<String>> rows = new ArrayList<>();
        for (Score score : records) {
            rows.add(List.of(
                    safe(score.getStudentNo()),
                    safe(score.getStudentName()),
                    safe(score.getClassName()),
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

        writeExport(response, ExportFormat.fromNullable(format), "\u6210\u7ee9\u5355", "score", headers, rows);
    }

    @GetMapping("/attendance")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public void exportAttendance(@RequestParam(required = false) String studentId,
                                 @RequestParam(required = false) Long courseArrangementId,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate attendanceDate,
                                 @RequestParam(required = false) Attendance.Status status,
                                 @RequestParam(required = false) String format,
                                 Authentication authentication,
                                 HttpServletResponse response) {
        String scopedStudentId = dataScopeService.resolveScopedStudentNo(authentication, studentId);
        Long scopedTeacherId = dataScopeService.resolveScopedTeacherId(authentication, null);
        dataScopeService.assertTeacherOwnsArrangement(authentication, courseArrangementId);
        Set<Long> scopedArrangementIds = resolveCollegeArrangementScope(authentication, courseArrangementId);
        Page<Attendance> result = attendanceService.getAttendancePage(
                1, EXPORT_LIMIT, scopedStudentId, scopedTeacherId, courseArrangementId, attendanceDate, status);
        List<Attendance> records = applyArrangementScopeForAttendance(result.getRecords(), scopedArrangementIds);

        List<String> headers = List.of(
                "\u5b66\u53f7",
                "\u59d3\u540d",
                "\u73ed\u7ea7",
                "\u8bfe\u7a0b",
                "\u8003\u52e4\u65e5\u671f",
                "\u72b6\u6001",
                "\u7b7e\u5230\u65f6\u95f4",
                "\u7b7e\u9000\u65f6\u95f4",
                "\u5907\u6ce8"
        );

        List<List<String>> rows = new ArrayList<>();
        for (Attendance attendance : records) {
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

        writeExport(response, ExportFormat.fromNullable(format), "\u8003\u52e4\u660e\u7ec6", "attendance", headers, rows);
    }

    @GetMapping("/leave-request")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public void exportLeaveRequest(@RequestParam(required = false) String studentId,
                                   @RequestParam(required = false) LeaveRequest.Status status,
                                   @RequestParam(required = false) String format,
                                   Authentication authentication,
                                   HttpServletResponse response) {
        String scopedStudentId = dataScopeService.resolveScopedStudentNo(authentication, studentId);
        Long scopedTeacherId = dataScopeService.resolveScopedTeacherId(authentication, null);
        Page<LeaveRequest> result;
        if (currentUserService.isCollegeAdmin(authentication)) {
            Set<String> studentIds = new HashSet<>(dataScopeService.resolveCollegeStudentNos(authentication));
            if (scopedStudentId != null) {
                studentIds = studentIds.contains(scopedStudentId) ? Set.of(scopedStudentId) : Set.of();
            }
            result = leaveRequestService.getLeaveRequestPageByStudentIds(1, EXPORT_LIMIT, studentIds, status);
        } else {
            result = leaveRequestService.getLeaveRequestPage(1, EXPORT_LIMIT, scopedStudentId, scopedTeacherId, status);
        }

        List<String> headers = List.of(
                "\u5b66\u53f7",
                "\u59d3\u540d",
                "\u73ed\u7ea7",
                "\u8bfe\u7a0b",
                "\u8bf7\u5047\u7c7b\u578b",
                "\u5f00\u59cb\u65f6\u95f4",
                "\u7ed3\u675f\u65f6\u95f4",
                "\u72b6\u6001",
                "\u5ba1\u6279\u4eba",
                "\u5ba1\u6279\u5907\u6ce8",
                "\u63d0\u4ea4\u65f6\u95f4"
        );

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

        writeExport(response, ExportFormat.fromNullable(format), "\u8bf7\u5047\u7edf\u8ba1", "leave-request", headers, rows);
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
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName
            );
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

    private Set<Long> resolveCollegeArrangementScope(Authentication authentication, Long requestedArrangementId) {
        if (!currentUserService.isCollegeAdmin(authentication)) {
            return null;
        }
        Set<String> classIds = dataScopeService.resolveCollegeClassCodes(authentication);
        if (classIds.isEmpty()) {
            return Set.of();
        }
        Set<Long> arrangementIds = courseArrangementMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CourseArrangement>()
                                .in(CourseArrangement::getClassId, classIds))
                .stream()
                .map(CourseArrangement::getId)
                .filter(id -> id != null)
                .collect(java.util.stream.Collectors.toSet());
        if (requestedArrangementId != null) {
            return arrangementIds.contains(requestedArrangementId) ? Set.of(requestedArrangementId) : Set.of();
        }
        return arrangementIds;
    }

    private List<Score> applyArrangementScope(List<Score> records, Set<Long> arrangementIds) {
        if (arrangementIds == null) {
            return records;
        }
        if (arrangementIds.isEmpty()) {
            return List.of();
        }
        return records.stream()
                .filter(item -> item.getCourseArrangementId() != null && arrangementIds.contains(item.getCourseArrangementId()))
                .toList();
    }

    private List<Attendance> applyArrangementScopeForAttendance(List<Attendance> records, Set<Long> arrangementIds) {
        if (arrangementIds == null) {
            return records;
        }
        if (arrangementIds.isEmpty()) {
            return List.of();
        }
        return records.stream()
                .filter(item -> item.getCourseArrangementId() != null && arrangementIds.contains(item.getCourseArrangementId()))
                .toList();
    }
}

