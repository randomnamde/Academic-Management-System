package com.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.dto.LeaveRequestDTO;
import com.student.entity.LeaveRequestCc;
import com.student.entity.LeaveRequest;
import com.student.security.RoleCode;

import java.util.List;
import java.util.Set;

public interface LeaveRequestService extends IService<LeaveRequest> {

    void submitLeaveRequest(LeaveRequestDTO leaveRequestDTO);

    void updateLeaveRequest(LeaveRequestDTO leaveRequestDTO);

    void cancelLeaveRequest(Long id);

    void approveLeaveRequest(Long id, boolean approved, String remark, Long approverUserId, Long approverTeacherId, Set<RoleCode> approverRoles);

    LeaveRequest getLeaveRequestById(Long id);

    Page<LeaveRequest> getLeaveRequestPage(Integer page, Integer size, Long studentId, Long teacherId,
                                          LeaveRequest.Status status);

    Page<LeaveRequest> getLeaveRequestPageByStudentIds(Integer page, Integer size, Set<Long> studentIds,
                                                       LeaveRequest.Status status);

    List<LeaveRequest> getStudentLeaveRequests(Long studentId);

    List<LeaveRequest> getPendingRequestsForTeacher(Long teacherId);

    List<LeaveRequest> getPendingRequestsForCollege(Long collegeId);

    List<LeaveRequestCc> getCcList(Long receiverUserId);

    void markCcRead(Long ccId, Long receiverUserId);
}
