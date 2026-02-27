package com.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.dto.LeaveRequestDTO;
import com.student.entity.LeaveRequest;

import java.util.List;

public interface LeaveRequestService extends IService<LeaveRequest> {

    void submitLeaveRequest(LeaveRequestDTO leaveRequestDTO);

    void updateLeaveRequest(LeaveRequestDTO leaveRequestDTO);

    void cancelLeaveRequest(Long id);

    void approveLeaveRequest(Long id, boolean approved, String remark, Long approverId);

    LeaveRequest getLeaveRequestById(Long id);

    Page<LeaveRequest> getLeaveRequestPage(Integer page, Integer size, Long studentId, 
                                          LeaveRequest.Status status);

    List<LeaveRequest> getStudentLeaveRequests(Long studentId);

    List<LeaveRequest> getPendingRequestsForTeacher(Long teacherId);
}
