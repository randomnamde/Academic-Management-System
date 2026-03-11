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

    void approveLeaveRequest(Long id, boolean approved, String remark, String approverUserId, Long approverTeacherId, Set<RoleCode> approverRoles);

    LeaveRequest getLeaveRequestById(Long id);

    Page<LeaveRequest> getLeaveRequestPage(Integer page, Integer size, String studentId, Long teacherId,
                                          LeaveRequest.Status status);

    Page<LeaveRequest> getLeaveRequestPageByStudentIds(Integer page, Integer size, Set<String> studentIds,
                                                       LeaveRequest.Status status);

    List<LeaveRequest> getStudentLeaveRequests(String studentId);

    List<LeaveRequest> getPendingRequestsForTeacher(Long teacherId);

    List<LeaveRequest> getPendingRequestsForCollege(Long collegeId);

    List<LeaveRequestCc> getCcList(String receiverUserId);

    void markCcRead(Long ccId, String receiverUserId);
}
