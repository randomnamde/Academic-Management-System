package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.CourseEvaluationDTO;
import com.student.entity.CourseEvaluation;
import com.student.service.CourseEvaluationService;
import com.student.vo.ResultVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/evaluation")
public class CourseEvaluationController {

    @Autowired
    private CourseEvaluationService evaluationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<Page<CourseEvaluation>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) Long courseArrangementId,
            @RequestParam(required = false) String teacherNo,
            @RequestParam(required = false) String status,
            Authentication authentication) {
        Page<CourseEvaluation> result = evaluationService.getEvaluationPage(page, size, studentId,
                courseArrangementId, teacherNo, status);
        return ResultVO.success(result);
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResultVO<CourseEvaluation> create(@Valid @RequestBody CourseEvaluationDTO dto,
                                               Authentication authentication) {
        String studentNo = authentication.getName();
        dto.setStudentId(studentNo);
        CourseEvaluation result = evaluationService.createEvaluation(dto);
        return ResultVO.success(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'COURSE_TEACHER')")
    public ResultVO<CourseEvaluation> update(@PathVariable Long id,
                                                @Valid @RequestBody CourseEvaluationDTO dto) {
        CourseEvaluation result = evaluationService.updateEvaluation(id, dto);
        return ResultVO.success(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'COURSE_TEACHER')")
    public ResultVO<Void> delete(@PathVariable Long id) {
        evaluationService.deleteEvaluation(id);
        return ResultVO.success(null);
    }

    @GetMapping("/teacher/{teacherNo}/statistics")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<Map<String, Object>> getTeacherStatistics(
            @PathVariable String teacherNo,
            @RequestParam(required = false) String semester) {
        Map<String, Object> result = evaluationService.getTeacherStatistics(teacherNo, semester);
        return ResultVO.success(result);
    }

    @GetMapping("/course/{courseArrangementId}/statistics")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<List<Map<String, Object>>> getCourseStatistics(
            @PathVariable Long courseArrangementId) {
        List<Map<String, Object>> result = evaluationService.getCourseStatistics(courseArrangementId);
        return ResultVO.success(result);
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Void> publish(@PathVariable Long id) {
        evaluationService.publishEvaluation(id);
        return ResultVO.success(null);
    }
}
