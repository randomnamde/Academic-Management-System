package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.LeaveRequestDTO;
import com.student.entity.LeaveRequest;
import com.student.entity.Student;
import com.student.entity.SysUser;
import com.student.entity.Teacher;
import com.student.exception.BusinessException;
import com.student.mapper.TeacherMapper;
import com.student.service.LeaveRequestService;
import com.student.service.StudentService;
import com.student.service.SysUserService;
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
    private final SysUserService sysUserService;
    private final StudentService studentService;
    private final TeacherMapper teacherMapper;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResultVO<Void> submit(@RequestBody @Validated LeaveRequestDTO leaveRequestDTO, Authentication authentication) {
        leaveRequestDTO.setStudentId(getCurrentStudentId(authentication));
        leaveRequestService.submitLeaveRequest(leaveRequestDTO);
        return ResultVO.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResultVO<Void> update(@PathVariable Long id, @RequestBody @Validated LeaveRequestDTO leaveRequestDTO, Authentication authentication) {
        Long studentId = getCurrentStudentId(authentication);
        validateStudentOwnsRequest(id, studentId);
        leaveRequestDTO.setId(id);
        leaveRequestDTO.setStudentId(studentId);
        leaveRequestService.updateLeaveRequest(leaveRequestDTO);
        return ResultVO.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResultVO<Void> cancel(@PathVariable Long id, Authentication authentication) {
        validateStudentOwnsRequest(id, getCurrentStudentId(authentication));
        leaveRequestService.cancelLeaveRequest(id);
        return ResultVO.success();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<Void> approve(@PathVariable Long id,
                                  @RequestParam boolean approved,
                                  @RequestParam(required = false) String remark,
                                  Authentication authentication) {
        Long approverId = getApproverId(authentication);
        leaveRequestService.approveLeaveRequest(id, approved, remark, approverId);
        return ResultVO.success();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<LeaveRequest> getById(@PathVariable Long id, Authentication authentication) {
        LeaveRequest leaveRequest = leaveRequestService.getLeaveRequestById(id);
        if (leaveRequest == null) {
            throw new BusinessException("Leave request not found");
        }
        SysUser currentUser = getCurrentUser(authentication);
        if (currentUser.getRole() == SysUser.Role.STUDENT && !getCurrentStudentId(authentication).equals(leaveRequest.getStudentId())) {
            throw new BusinessException("No permission to view this leave request");
        }
        return ResultVO.success(leaveRequest);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<Page<LeaveRequest>> list(@RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer size,
                                             @RequestParam(required = false) Long studentId,
                                             @RequestParam(required = false) LeaveRequest.Status status,
                                             Authentication authentication) {
        SysUser currentUser = getCurrentUser(authentication);
        if (currentUser.getRole() == SysUser.Role.STUDENT) {
            studentId = getCurrentStudentId(authentication);
        }
        Page<LeaveRequest> result = leaveRequestService.getLeaveRequestPage(page, size, studentId, status);
        return ResultVO.success(result);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<List<LeaveRequest>> getByStudentId(@PathVariable Long studentId, Authentication authentication) {
        SysUser currentUser = getCurrentUser(authentication);
        if (currentUser.getRole() == SysUser.Role.STUDENT && !getCurrentStudentId(authentication).equals(studentId)) {
            throw new BusinessException("No permission to view other students leave requests");
        }
        List<LeaveRequest> leaveRequests = leaveRequestService.getStudentLeaveRequests(studentId);
        return ResultVO.success(leaveRequests);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<List<LeaveRequest>> getPending(Authentication authentication) {
        SysUser currentUser = getCurrentUser(authentication);
        if (currentUser.getRole() == SysUser.Role.ADMIN) {
            Page<LeaveRequest> page = leaveRequestService.getLeaveRequestPage(1, 200, null, LeaveRequest.Status.PENDING);
            return ResultVO.success(page.getRecords());
        }

        Long teacherId = getCurrentTeacherId(authentication);
        List<LeaveRequest> leaveRequests = leaveRequestService.getPendingRequestsForTeacher(teacherId);
        return ResultVO.success(leaveRequests);
    }

    private SysUser getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new BusinessException("Unauthorized");
        }
        SysUser user = sysUserService.getByUsername(authentication.getName());
        if (user == null) {
            throw new BusinessException("User not found");
        }
        return user;
    }

    private Long getCurrentStudentId(Authentication authentication) {
        SysUser user = getCurrentUser(authentication);
        Student student = studentService.getStudentByUserId(user.getId());
        if (student == null) {
            throw new BusinessException("Student profile not found");
        }
        return student.getId();
    }

    private Long getCurrentTeacherId(Authentication authentication) {
        SysUser user = getCurrentUser(authentication);
        Teacher teacher = teacherMapper.selectByUserId(user.getId());
        if (teacher == null) {
            throw new BusinessException("Teacher profile not found");
        }
        return teacher.getId();
    }

    private Long getApproverId(Authentication authentication) {
        SysUser user = getCurrentUser(authentication);
        if (user.getRole() == SysUser.Role.TEACHER) {
            return getCurrentTeacherId(authentication);
        }
        return user.getId();
    }

    private void validateStudentOwnsRequest(Long requestId, Long studentId) {
        LeaveRequest leaveRequest = leaveRequestService.getById(requestId);
        if (leaveRequest == null) {
            throw new BusinessException("Leave request not found");
        }
        if (!studentId.equals(leaveRequest.getStudentId())) {
            throw new BusinessException("No permission to modify this leave request");
        }
    }
}
