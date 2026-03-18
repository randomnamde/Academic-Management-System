package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.CollegeDTO;
import com.student.entity.College;
import com.student.security.CurrentUserService;
import com.student.service.CollegeService;
import com.student.service.SysUserService;
import com.student.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/college")
@RequiredArgsConstructor
public class CollegeController {

    private final CollegeService collegeService;
    private final CurrentUserService currentUserService;
    private final SysUserService sysUserService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Page<College>> list(@RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer size,
                                        @RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) Integer status,
                                        Authentication authentication) {
        String scopedCollegeCode = currentUserService.resolveManagedCollegeCode(authentication);
        Page<College> result = collegeService.getCollegePage(page, size, keyword, status, scopedCollegeCode);
        return ResultVO.success(result);
    }

    @GetMapping("/{collegeCode}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<College> detail(@PathVariable String collegeCode, Authentication authentication) {
        College college = collegeService.getById(collegeCode);
        if (college == null) {
            return ResultVO.error(404, "College not found");
        }
        String scopedCollegeCode = currentUserService.resolveManagedCollegeCode(authentication);
        if (scopedCollegeCode != null && !scopedCollegeCode.equals(collegeCode)) {
            return ResultVO.error(403, "Forbidden");
        }
        if (college.getAdminUserId() != null) {
            var adminUser = sysUserService.getByUsername(college.getAdminUserId());
            college.setAdminUsername(adminUser != null ? adminUser.getUsername() : null);
        }
        return ResultVO.success(college);
    }

    @PostMapping
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResultVO<College> create(@RequestBody @Validated CollegeDTO dto) {
        return ResultVO.success(collegeService.createCollege(dto));
    }

    @PutMapping("/{collegeCode}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Void> update(@PathVariable String collegeCode,
                                 @RequestBody @Validated CollegeDTO dto,
                                 Authentication authentication) {
        String scopedCollegeCode = currentUserService.resolveManagedCollegeCode(authentication);
        collegeService.updateCollege(collegeCode, dto, scopedCollegeCode);
        return ResultVO.success();
    }

    @PutMapping("/{collegeCode}/status")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Void> updateStatus(@PathVariable String collegeCode,
                                       @RequestParam Integer status,
                                       Authentication authentication) {
        String scopedCollegeCode = currentUserService.resolveManagedCollegeCode(authentication);
        collegeService.updateCollegeStatus(collegeCode, status, scopedCollegeCode);
        return ResultVO.success();
    }

    @PutMapping("/{collegeCode}/admin")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Void> bindAdmin(@PathVariable String collegeCode,
                                    @RequestParam String adminUsername,
                                    Authentication authentication) {
        String scopedCollegeCode = currentUserService.resolveManagedCollegeCode(authentication);
        collegeService.bindAdmin(collegeCode, adminUsername, scopedCollegeCode);
        return ResultVO.success();
    }
}