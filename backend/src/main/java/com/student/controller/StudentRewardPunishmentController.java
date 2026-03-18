package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.StudentRewardPunishmentDTO;
import com.student.entity.StudentRewardPunishment;
import com.student.service.StudentRewardPunishmentService;
import com.student.vo.ResultVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reward-punishment")
public class StudentRewardPunishmentController {

    @Autowired
    private StudentRewardPunishmentService recordService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'STUDENT')")
    public ResultVO<Page<StudentRewardPunishment>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category) {
        Page<StudentRewardPunishment> result = recordService.getRecordPage(page, size, studentId,
                type, status, category);
        return ResultVO.success(result);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER')")
    public ResultVO<StudentRewardPunishment> create(@Valid @RequestBody StudentRewardPunishmentDTO dto) {
        StudentRewardPunishment result = recordService.createRecord(dto);
        return ResultVO.success(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER')")
    public ResultVO<StudentRewardPunishment> update(@PathVariable Long id,
                                                      @Valid @RequestBody StudentRewardPunishmentDTO dto) {
        StudentRewardPunishment result = recordService.updateRecord(id, dto);
        return ResultVO.success(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Void> delete(@PathVariable Long id) {
        recordService.deleteRecord(id);
        return ResultVO.success(null);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Void> approve(@PathVariable Long id, Authentication authentication) {
        String approverNo = authentication.getName();
        recordService.approveRecord(id, approverNo);
        return ResultVO.success(null);
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Void> reject(@PathVariable Long id,
                                   @RequestParam(required = false) String reason,
                                   Authentication authentication) {
        String approverNo = authentication.getName();
        recordService.rejectRecord(id, approverNo, reason);
        return ResultVO.success(null);
    }
}
