package com.student.controller;

import com.student.security.CurrentUserService;
import com.student.service.TeacherWorkloadService;
import com.student.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/teacher/workload")
@RequiredArgsConstructor
public class TeacherWorkloadController {

    private final TeacherWorkloadService teacherWorkloadService;
    private final CurrentUserService currentUserService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<Map<String, Object>> getMyWorkload(
            @RequestParam(required = false) String semester,
            Authentication authentication) {
        String teacherNo = currentUserService.getCurrentTeacherNo(authentication);
        Map<String, Object> workload = teacherWorkloadService.getTeacherWorkload(teacherNo, semester);
        return ResultVO.success(workload);
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<Map<String, Object>> getMyWorkloadSummary(
            @RequestParam(required = false) String semester,
            Authentication authentication) {
        String teacherNo = currentUserService.getCurrentTeacherNo(authentication);
        Map<String, Object> summary = teacherWorkloadService.getWorkloadSummary(teacherNo, semester);
        return ResultVO.success(summary);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<List<Map<String, Object>>> getTeachingStatistics(
            @RequestParam(required = false) String collegeCode,
            @RequestParam(required = false) String semester) {
        List<Map<String, Object>> statistics = teacherWorkloadService.getTeachingStatistics(collegeCode, semester);
        return ResultVO.success(statistics);
    }

    @GetMapping("/teacher/{teacherNo}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Map<String, Object>> getTeacherWorkload(
            @PathVariable String teacherNo,
            @RequestParam(required = false) String semester) {
        Map<String, Object> workload = teacherWorkloadService.getTeacherWorkload(teacherNo, semester);
        return ResultVO.success(workload);
    }
}
