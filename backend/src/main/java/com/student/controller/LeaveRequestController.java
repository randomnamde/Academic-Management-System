package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.LeaveRequestDTO;
import com.student.entity.LeaveRequest;
import com.student.service.LeaveRequestService;
import com.student.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leave-request")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResultVO<Void> submit(@RequestBody @Validated LeaveRequestDTO leaveRequestDTO) {
        leaveRequestService.submitLeaveRequest(leaveRequestDTO);
        return ResultVO.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResultVO<Void> update(@PathVariable Long id, @RequestBody @Validated LeaveRequestDTO leaveRequestDTO) {
        leaveRequestDTO.setId(id);
        leaveRequestService.updateLeaveRequest(leaveRequestDTO);
        return ResultVO.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResultVO<Void> cancel(@PathVariable Long id) {
        leaveRequestService.cancelLeaveRequest(id);
        return ResultVO.success();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<Void> approve(@PathVariable Long id,
                                   @RequestParam boolean approved,
                                   @RequestParam(required = false) String remark,
                                   Authentication authentication) {
        // 从认证信息中获取审批人ID
        Long approverId = getUserIdFromAuthentication(authentication);
        leaveRequestService.approveLeaveRequest(id, approved, remark, approverId);
        return ResultVO.success();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<LeaveRequest> getById(@PathVariable Long id) {
        LeaveRequest leaveRequest = leaveRequestService.getLeaveRequestById(id);
        return ResultVO.success(leaveRequest);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<Page<LeaveRequest>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) LeaveRequest.Status status) {
        Page<LeaveRequest> result = leaveRequestService.getLeaveRequestPage(page, size, studentId, status);
        return ResultVO.success(result);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<List<LeaveRequest>> getByStudentId(@PathVariable Long studentId) {
        List<LeaveRequest> leaveRequests = leaveRequestService.getStudentLeaveRequests(studentId);
        return ResultVO.success(leaveRequests);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<List<LeaveRequest>> getPending(Authentication authentication) {
        Long teacherId = getUserIdFromAuthentication(authentication);
        List<LeaveRequest> leaveRequests = leaveRequestService.getPendingRequestsForTeacher(teacherId);
        return ResultVO.success(leaveRequests);
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        // 这里需要根据实际情况从 authentication 中获取用户ID
        // 简化处理，实际应该通过 userService 获取
        return 1L; // 临时返回
    }
}
