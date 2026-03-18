package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.LeaveRequestDTO;
import com.student.entity.LeaveRequest;
import com.student.entity.LeaveRequestCc;
import com.student.security.CurrentUserService;
import com.student.security.DataScopeService;
import com.student.security.RoleCode;
import com.student.service.LeaveRequestService;
import com.student.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/leave-request")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;
    private final CurrentUserService currentUserService;
    private final DataScopeService dataScopeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT')")
    public ResultVO<Void> submit(@RequestBody @Validated LeaveRequestDTO leaveRequestDTO, Authentication authentication) {
        leaveRequestDTO.setStudentId(currentUserService.getCurrentStudentNo(authentication));
        leaveRequestService.submitLeaveRequest(leaveRequestDTO);
        return ResultVO.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT')")
    public ResultVO<Void> update(@PathVariable Long id, @RequestBody @Validated LeaveRequestDTO leaveRequestDTO, Authentication authentication) {
        String studentId = currentUserService.getCurrentStudentNo(authentication);
        validateStudentOwnsRequest(id, studentId);
        leaveRequestDTO.setId(id);
        leaveRequestDTO.setStudentId(studentId);
        leaveRequestService.updateLeaveRequest(leaveRequestDTO);
        return ResultVO.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT')")
    public ResultVO<Void> cancel(@PathVariable Long id, Authentication authentication) {
        validateStudentOwnsRequest(id, currentUserService.getCurrentStudentNo(authentication));
        leaveRequestService.cancelLeaveRequest(id);
        return ResultVO.success();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER')")
    public ResultVO<Void> approve(@PathVariable Long id,
                                  @RequestParam boolean approved,
                                  @RequestParam(required = false) String remark,
                                  Authentication authentication) {
        String approverUserId = currentUserService.getCurrentUser(authentication).getUsername();
        String approverTeacherNo = null;
        if (currentUserService.isTeacher(authentication)) {
            approverTeacherNo = currentUserService.getCurrentTeacherNo(authentication);
        }
        Set<RoleCode> roles = currentUserService.getCurrentRoleCodes(authentication);
        leaveRequestService.approveLeaveRequest(id, approved, remark, approverUserId, approverTeacherNo, roles);
        return ResultVO.success();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<LeaveRequest> getById(@PathVariable Long id, Authentication authentication) {
        LeaveRequest leaveRequest = leaveRequestService.getLeaveRequestById(id);
        if (leaveRequest == null) {
            return ResultVO.error(404, "Leave request not found");
        }
        if (currentUserService.isStudent(authentication)
                && !currentUserService.getCurrentStudentNo(authentication).equals(leaveRequest.getStudentId())) {
            return ResultVO.error(403, "Forbidden");
        }
        if (currentUserService.isCollegeAdmin(authentication)) {
            Set<String> scopedStudentIds = dataScopeService.resolveCollegeStudentNos(authentication);
            if (!scopedStudentIds.contains(leaveRequest.getStudentId())) {
                return ResultVO.error(403, "Forbidden");
            }
        }
        if (!currentUserService.isAdmin(authentication)
                && !currentUserService.isCollegeAdmin(authentication)
                && currentUserService.isTeacher(authentication)) {
            String teacherNo = currentUserService.getCurrentTeacherNo(authentication);
            Page<LeaveRequest> scopedPage = leaveRequestService.getLeaveRequestPage(
                    1, 20, leaveRequest.getStudentId(), teacherNo, null);
            boolean canView = scopedPage.getRecords().stream().anyMatch(item -> id.equals(item.getId()));
            if (!canView) {
                String userId = currentUserService.getCurrentUser(authentication).getUsername();
                canView = leaveRequestService.getCcList(userId).stream().anyMatch(item -> id.equals(item.getLeaveRequestId()));
            }
            if (!canView) {
                return ResultVO.error(403, "Forbidden");
            }
        }
        return ResultVO.success(leaveRequest);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<Page<LeaveRequest>> list(@RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer size,
                                             @RequestParam(required = false) String studentId,
                                             @RequestParam(required = false) LeaveRequest.Status status,
                                             Authentication authentication) {
        String scopedStudentId = studentId;
        if (currentUserService.isStudent(authentication)) {
            scopedStudentId = currentUserService.getCurrentStudentNo(authentication);
        }
        if (currentUserService.isCollegeAdmin(authentication)) {
            Set<String> studentIds = new HashSet<>(dataScopeService.resolveCollegeStudentNos(authentication));
            if (scopedStudentId != null) {
                if (!studentIds.contains(scopedStudentId)) {
                    Page<LeaveRequest> emptyPage = new Page<>(page, size, 0);
                    emptyPage.setRecords(List.of());
                    return ResultVO.success(emptyPage);
                }
                studentIds = Set.of(scopedStudentId);
            }
            Page<LeaveRequest> result = leaveRequestService.getLeaveRequestPageByStudentIds(page, size, studentIds, status);
            return ResultVO.success(result);
        }
        String scopedTeacherNo = null;
        if (currentUserService.hasRole(authentication, RoleCode.HOMEROOM_TEACHER)
                || currentUserService.hasRole(authentication, RoleCode.COURSE_TEACHER)) {
            scopedTeacherNo = currentUserService.getCurrentTeacherNo(authentication);
        }
        Page<LeaveRequest> result = leaveRequestService.getLeaveRequestPage(page, size, scopedStudentId, scopedTeacherNo, status);
        return ResultVO.success(result);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<List<LeaveRequest>> getByStudentId(@PathVariable String studentId, Authentication authentication) {
        if (currentUserService.isStudent(authentication)
                && !currentUserService.getCurrentStudentNo(authentication).equals(studentId)) {
            return ResultVO.error(403, "Forbidden");
        }
        if (currentUserService.isCollegeAdmin(authentication)) {
            Set<String> scopedStudentIds = dataScopeService.resolveCollegeStudentNos(authentication);
            if (!scopedStudentIds.contains(studentId)) {
                return ResultVO.error(403, "Forbidden");
            }
        }
        String scopedTeacherNo = null;
        if (currentUserService.hasRole(authentication, RoleCode.HOMEROOM_TEACHER)
                || currentUserService.hasRole(authentication, RoleCode.COURSE_TEACHER)) {
            scopedTeacherNo = currentUserService.getCurrentTeacherNo(authentication);
        }
        if (scopedTeacherNo != null) {
            Page<LeaveRequest> result = leaveRequestService.getLeaveRequestPage(1, 10000, studentId, scopedTeacherNo, null);
            return ResultVO.success(result.getRecords());
        }
        List<LeaveRequest> leaveRequests = leaveRequestService.getStudentLeaveRequests(studentId);
        return ResultVO.success(leaveRequests);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER')")
    public ResultVO<List<LeaveRequest>> getPending(Authentication authentication) {
        if (currentUserService.isAdmin(authentication)) {
            Page<LeaveRequest> page = leaveRequestService.getLeaveRequestPage(1, 200, null, null, LeaveRequest.Status.PENDING);
            return ResultVO.success(page.getRecords());
        }
        if (currentUserService.isCollegeAdmin(authentication)) {
            String collegeCode = currentUserService.resolveManagedCollegeCode(authentication);
            return ResultVO.success(leaveRequestService.getPendingRequestsForCollege(collegeCode));
        }
        if (currentUserService.hasRole(authentication, RoleCode.HOMEROOM_TEACHER)) {
            String teacherNo = currentUserService.getCurrentTeacherNo(authentication);
            return ResultVO.success(leaveRequestService.getPendingRequestsForTeacher(teacherNo));
        }
        return ResultVO.success(List.of());
    }

    @GetMapping("/cc")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<List<LeaveRequestCc>> getCc(Authentication authentication) {
        String userId = currentUserService.getCurrentUser(authentication).getUsername();
        return ResultVO.success(leaveRequestService.getCcList(userId));
    }

    @PutMapping("/cc/{id}/read")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<Void> readCc(@PathVariable Long id, Authentication authentication) {
        String userId = currentUserService.getCurrentUser(authentication).getUsername();
        leaveRequestService.markCcRead(id, userId);
        return ResultVO.success();
    }

    private void validateStudentOwnsRequest(Long requestId, String studentId) {
        LeaveRequest leaveRequest = leaveRequestService.getById(requestId);
        if (leaveRequest == null) {
            throw new com.student.exception.BusinessException("Leave request not found");
        }
        if (!studentId.equals(leaveRequest.getStudentId())) {
            throw new com.student.exception.BusinessException("No permission to modify this leave request");
        }
    }
}



