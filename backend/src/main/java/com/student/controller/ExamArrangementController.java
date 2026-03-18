package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.ExamArrangementDTO;
import com.student.entity.ExamArrangement;
import com.student.service.ExamArrangementService;
import com.student.vo.ResultVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/exam")
public class ExamArrangementController {

    @Autowired
    private ExamArrangementService examService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<Page<ExamArrangement>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long courseArrangementId,
            @RequestParam(required = false) String examType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String examDate) {
        Page<ExamArrangement> result = examService.getExamPage(page, size, courseArrangementId,
                examType, status, examDate);
        return ResultVO.success(result);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<ExamArrangement> create(@Valid @RequestBody ExamArrangementDTO dto) {
        ExamArrangement result = examService.createExam(dto);
        return ResultVO.success(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<ExamArrangement> update(@PathVariable Long id,
                                               @Valid @RequestBody ExamArrangementDTO dto) {
        ExamArrangement result = examService.updateExam(id, dto);
        return ResultVO.success(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Void> delete(@PathVariable Long id) {
        examService.deleteExam(id);
        return ResultVO.success(null);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        examService.updateStatus(id, status);
        return ResultVO.success(null);
    }
}
