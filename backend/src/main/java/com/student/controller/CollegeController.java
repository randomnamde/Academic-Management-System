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
        Long scopedCollegeId = currentUserService.resolveManagedCollegeId(authentication);
        Page<College> result = collegeService.getCollegePage(page, size, keyword, status, scopedCollegeId);
        return ResultVO.success(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<College> detail(@PathVariable Long id, Authentication authentication) {
        College college = collegeService.getById(id);
        if (college == null) {
            return ResultVO.error(404, "学院不存在");
        }
        Long scopedCollegeId = currentUserService.resolveManagedCollegeId(authentication);
        if (scopedCollegeId != null && !scopedCollegeId.equals(id)) {
            return ResultVO.error(403, "无权查看该学院");
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

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Void> update(@PathVariable Long id,
                                 @RequestBody @Validated CollegeDTO dto,
                                 Authentication authentication) {
        Long scopedCollegeId = currentUserService.resolveManagedCollegeId(authentication);
        collegeService.updateCollege(id, dto, scopedCollegeId);
        return ResultVO.success();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Void> updateStatus(@PathVariable Long id,
                                       @RequestParam Integer status,
                                       Authentication authentication) {
        Long scopedCollegeId = currentUserService.resolveManagedCollegeId(authentication);
        collegeService.updateCollegeStatus(id, status, scopedCollegeId);
        return ResultVO.success();
    }

    @PutMapping("/{id}/admin")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Void> bindAdmin(@PathVariable Long id,
                                    @RequestParam String adminUsername,
                                    Authentication authentication) {
        Long scopedCollegeId = currentUserService.resolveManagedCollegeId(authentication);
        collegeService.bindAdmin(id, adminUsername, scopedCollegeId);
        return ResultVO.success();
    }
}


