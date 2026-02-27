package com.student.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.dto.LeaveRequestDTO;
import com.student.entity.LeaveRequest;
import com.student.exception.BusinessException;
import com.student.mapper.LeaveRequestMapper;
import com.student.service.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveRequestServiceImpl extends ServiceImpl<LeaveRequestMapper, LeaveRequest> implements LeaveRequestService {

    private final LeaveRequestMapper leaveRequestMapper;

    @Override
    @Transactional
    public void submitLeaveRequest(LeaveRequestDTO leaveRequestDTO) {
        if (leaveRequestDTO.getStartTime().isAfter(leaveRequestDTO.getEndTime())) {
            throw new BusinessException("Start time cannot be after end time");
        }

        LeaveRequest leaveRequest = new LeaveRequest();
        BeanUtils.copyProperties(leaveRequestDTO, leaveRequest);
        leaveRequest.setStatus(LeaveRequest.Status.PENDING);
        leaveRequestMapper.insert(leaveRequest);
    }

    @Override
    @Transactional
    public void updateLeaveRequest(LeaveRequestDTO leaveRequestDTO) {
        if (leaveRequestDTO.getId() == null) {
            throw new BusinessException("Leave request id cannot be null");
        }

        LeaveRequest existRequest = leaveRequestMapper.selectById(leaveRequestDTO.getId());
        if (existRequest == null) {
            throw new BusinessException("Leave request not found");
        }

        if (existRequest.getStatus() != LeaveRequest.Status.PENDING) {
            throw new BusinessException("Only pending requests can be updated");
        }

        if (leaveRequestDTO.getStartTime().isAfter(leaveRequestDTO.getEndTime())) {
            throw new BusinessException("Start time cannot be after end time");
        }

        LeaveRequest leaveRequest = new LeaveRequest();
        BeanUtils.copyProperties(leaveRequestDTO, leaveRequest);
        leaveRequest.setStatus(LeaveRequest.Status.PENDING);
        leaveRequestMapper.updateById(leaveRequest);
    }

    @Override
    @Transactional
    public void cancelLeaveRequest(Long id) {
        LeaveRequest leaveRequest = leaveRequestMapper.selectById(id);
        if (leaveRequest == null) {
            throw new BusinessException("Leave request not found");
        }

        if (leaveRequest.getStatus() != LeaveRequest.Status.PENDING) {
            throw new BusinessException("Only pending requests can be cancelled");
        }

        leaveRequestMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void approveLeaveRequest(Long id, boolean approved, String remark, Long approverId) {
        LeaveRequest leaveRequest = leaveRequestMapper.selectById(id);
        if (leaveRequest == null) {
            throw new BusinessException("Leave request not found");
        }

        if (leaveRequest.getStatus() != LeaveRequest.Status.PENDING) {
            throw new BusinessException("This request has already been processed");
        }

        leaveRequest.setStatus(approved ? LeaveRequest.Status.APPROVED : LeaveRequest.Status.REJECTED);
        leaveRequest.setApproverId(approverId);
        leaveRequest.setApproveTime(LocalDateTime.now());
        leaveRequest.setApproveRemark(remark);
        leaveRequestMapper.updateById(leaveRequest);
    }

    @Override
    public LeaveRequest getLeaveRequestById(Long id) {
        return leaveRequestMapper.selectByIdWithDetail(id);
    }

    @Override
    public Page<LeaveRequest> getLeaveRequestPage(Integer page, Integer size, Long studentId, LeaveRequest.Status status) {
        Page<LeaveRequest> pageParam = new Page<>(page, size);
        return leaveRequestMapper.selectPageWithDetail(pageParam, studentId, status);
    }

    @Override
    public List<LeaveRequest> getStudentLeaveRequests(Long studentId) {
        return leaveRequestMapper.selectByStudentId(studentId);
    }

    @Override
    public List<LeaveRequest> getPendingRequestsForTeacher(Long teacherId) {
        return leaveRequestMapper.selectPendingByTeacherId(teacherId);
    }
}
