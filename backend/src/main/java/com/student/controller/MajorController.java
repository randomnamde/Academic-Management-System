package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.MajorDTO;
import com.student.entity.Major;
import com.student.security.CurrentUserService;
import com.student.service.MajorService;
import com.student.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/major")
@RequiredArgsConstructor
public class MajorController {

    private final MajorService majorService;
    private final CurrentUserService currentUserService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<Page<Major>> list(@RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer size,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) String collegeCode,
                                      @RequestParam(required = false) Integer status,
                                      Authentication authentication) {
        String scopedCollegeCode = currentUserService.resolveManagedCollegeCode(authentication);
        return ResultVO.success(majorService.getMajorPage(page, size, keyword, collegeCode, status, scopedCollegeCode));
    }

    @GetMapping("/options")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<List<Major>> options(@RequestParam(required = false) String collegeCode,
                                         @RequestParam(required = false) Integer status,
                                         Authentication authentication) {
        String scopedCollegeCode = currentUserService.resolveManagedCollegeCode(authentication);
        return ResultVO.success(majorService.getMajorOptions(collegeCode, status, scopedCollegeCode));
    }

    @GetMapping("/{majorCode}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Major> detail(@PathVariable String majorCode, Authentication authentication) {
        String scopedCollegeCode = currentUserService.resolveManagedCollegeCode(authentication);
        Major major = majorService.getMajorDetail(majorCode, scopedCollegeCode);
        if (major == null) {
            return ResultVO.error(404, "Major not found");
        }
        return ResultVO.success(major);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Major> create(@RequestBody @Validated MajorDTO dto, Authentication authentication) {
        String scopedCollegeCode = currentUserService.resolveManagedCollegeCode(authentication);
        return ResultVO.success(majorService.createMajor(dto, scopedCollegeCode));
    }

    @PutMapping("/{majorCode}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Void> update(@PathVariable String majorCode,
                                 @RequestBody @Validated MajorDTO dto,
                                 Authentication authentication) {
        String scopedCollegeCode = currentUserService.resolveManagedCollegeCode(authentication);
        majorService.updateMajor(majorCode, dto, scopedCollegeCode);
        return ResultVO.success();
    }

    @PutMapping("/{majorCode}/status")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Void> updateStatus(@PathVariable String majorCode,
                                       @RequestParam Integer status,
                                       Authentication authentication) {
        String scopedCollegeCode = currentUserService.resolveManagedCollegeCode(authentication);
        majorService.updateMajorStatus(majorCode, status, scopedCollegeCode);
        return ResultVO.success();
    }
}


