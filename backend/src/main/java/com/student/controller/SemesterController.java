package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.SemesterDTO;
import com.student.dto.SemesterStatusDTO;
import com.student.entity.Semester;
import com.student.service.SemesterService;
import com.student.vo.ResultVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/semester")
@RequiredArgsConstructor
public class SemesterController {

    private final SemesterService semesterService;

    @GetMapping
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResultVO<Page<Semester>> list(@RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "10") Integer size,
                                         @RequestParam(required = false) String semesterCode,
                                         @RequestParam(required = false) Semester.Status status) {
        return ResultVO.success(semesterService.getSemesterPage(page, size, semesterCode, status));
    }

    @PostMapping
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResultVO<Semester> add(@RequestBody @Valid SemesterDTO dto) {
        return ResultVO.success(semesterService.addSemester(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResultVO<Semester> update(@PathVariable Long id, @RequestBody @Valid SemesterDTO dto) {
        return ResultVO.success(semesterService.updateSemester(id, dto));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResultVO<Semester> updateStatus(@PathVariable Long id, @RequestBody @Valid SemesterStatusDTO dto) {
        return ResultVO.success(semesterService.updateSemesterStatus(id, dto.getStatus()));
    }

    @GetMapping("/options")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<List<Semester>> options() {
        return ResultVO.success(semesterService.getSemesterOptions());
    }
}
