package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.AlumniDTO;
import com.student.entity.Alumni;
import com.student.security.CurrentUserService;
import com.student.service.AlumniService;
import com.student.vo.ResultVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/alumni")
public class AlumniController {

    @Autowired
    private AlumniService alumniService;

    @Autowired
    private CurrentUserService currentUserService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Page<Alumni>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer graduationYear,
            @RequestParam(required = false) String collegeCode,
            @RequestParam(required = false) String majorCode,
            Authentication authentication) {
        String userCollegeCode = currentUserService.getUserCollegeCode(authentication);
        Page<Alumni> result = alumniService.getAlumniPage(page, size, studentId, name,
                graduationYear, userCollegeCode, majorCode);
        return ResultVO.success(result);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'STUDENT')")
    public ResultVO<Alumni> getByStudentId(@PathVariable String studentId) {
        Alumni result = alumniService.getByStudentId(studentId);
        return ResultVO.success(result);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Alumni> create(@Valid @RequestBody AlumniDTO dto) {
        Alumni result = alumniService.createAlumni(dto);
        return ResultVO.success(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Alumni> update(@PathVariable Long id, @Valid @RequestBody AlumniDTO dto) {
        Alumni result = alumniService.updateAlumni(id, dto);
        return ResultVO.success(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN')")
    public ResultVO<Void> delete(@PathVariable Long id) {
        alumniService.deleteAlumni(id);
        return ResultVO.success(null);
    }
}
