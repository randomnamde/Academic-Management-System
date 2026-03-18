package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.StudentEmploymentDTO;
import com.student.entity.StudentEmployment;
import com.student.security.CurrentUserService;
import com.student.service.StudentEmploymentService;
import com.student.vo.ResultVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/employment")
public class EmploymentController {

    @Autowired
    private StudentEmploymentService employmentService;

    @Autowired
    private CurrentUserService currentUserService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'STUDENT')")
    public ResultVO<Page<StudentEmployment>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) Integer graduationYear,
            @RequestParam(required = false) String employmentStatus,
            @RequestParam(required = false) String collegeCode,
            Authentication authentication) {
        String userCollegeCode = currentUserService.getUserCollegeCode(authentication);
        Page<StudentEmployment> result = employmentService.getEmploymentPage(page, size, studentId,
                graduationYear, employmentStatus, userCollegeCode);
        return ResultVO.success(result);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'STUDENT')")
    public ResultVO<StudentEmployment> getByStudentId(@PathVariable String studentId) {
        StudentEmployment result = employmentService.getByStudentId(studentId);
        return ResultVO.success(result);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER')")
    public ResultVO<StudentEmployment> create(@Valid @RequestBody StudentEmploymentDTO dto,
                                               Authentication authentication) {
        String recorderNo = authentication.getName();
        dto.setRecorderNo(recorderNo);
        StudentEmployment result = employmentService.createEmployment(dto);
        return ResultVO.success(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER')")
    public ResultVO<StudentEmployment> update(@PathVariable Long id,
                                                @Valid @RequestBody StudentEmploymentDTO dto) {
        StudentEmployment result = employmentService.updateEmployment(id, dto);
        return ResultVO.success(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Void> delete(@PathVariable Long id) {
        employmentService.deleteEmployment(id);
        return ResultVO.success(null);
    }

    @GetMapping("/statistics/year")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<List<Map<String, Object>>> getStatisticsByYear(
            @RequestParam(required = false) Integer graduationYear,
            @RequestParam(required = false) String collegeCode,
            Authentication authentication) {
        String userCollegeCode = currentUserService.getUserCollegeCode(authentication);
        List<Map<String, Object>> result = employmentService.getStatisticsByYear(graduationYear, userCollegeCode);
        return ResultVO.success(result);
    }

    @GetMapping("/statistics/industry")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<List<Map<String, Object>>> getStatisticsByIndustry(
            @RequestParam(required = false) Integer graduationYear,
            @RequestParam(required = false) String collegeCode,
            Authentication authentication) {
        String userCollegeCode = currentUserService.getUserCollegeCode(authentication);
        List<Map<String, Object>> result = employmentService.getStatisticsByIndustry(graduationYear, userCollegeCode);
        return ResultVO.success(result);
    }

    @GetMapping("/statistics/company-type")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<List<Map<String, Object>>> getStatisticsByCompanyType(
            @RequestParam(required = false) Integer graduationYear,
            @RequestParam(required = false) String collegeCode,
            Authentication authentication) {
        String userCollegeCode = currentUserService.getUserCollegeCode(authentication);
        List<Map<String, Object>> result = employmentService.getStatisticsByCompanyType(graduationYear, userCollegeCode);
        return ResultVO.success(result);
    }

    @GetMapping("/statistics/employment-rate")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Map<String, Object>> getEmploymentRate(
            @RequestParam(required = false) Integer graduationYear,
            @RequestParam(required = false) String collegeCode,
            Authentication authentication) {
        String userCollegeCode = currentUserService.getUserCollegeCode(authentication);
        Map<String, Object> result = employmentService.getEmploymentRate(graduationYear, userCollegeCode);
        return ResultVO.success(result);
    }
}
