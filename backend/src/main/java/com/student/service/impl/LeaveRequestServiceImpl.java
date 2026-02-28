package com.student.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.dto.LeaveRequestDTO;
import com.student.entity.Attendance;
import com.student.entity.LeaveRequest;
import com.student.exception.BusinessException;
import com.student.mapper.AttendanceMapper;
import com.student.mapper.LeaveRequestMapper;
import com.student.service.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveRequestServiceImpl extends ServiceImpl<LeaveRequestMapper, LeaveRequest> implements LeaveRequestService {

    private final LeaveRequestMapper leaveRequestMapper;
    private final AttendanceMapper attendanceMapper;

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

        if (approved) {
            syncAttendanceForApprovedLeave(leaveRequest);
        }
    }

    @Override
    public LeaveRequest getLeaveRequestById(Long id) {
        return leaveRequestMapper.selectByIdWithDetail(id);
    }

    @Override
    public Page<LeaveRequest> getLeaveRequestPage(Integer page, Integer size, Long studentId, Long teacherId, LeaveRequest.Status status) {
        Page<LeaveRequest> pageParam = new Page<>(page, size);
        return leaveRequestMapper.selectPageWithDetail(pageParam, studentId, teacherId, status);
    }

    @Override
    public List<LeaveRequest> getStudentLeaveRequests(Long studentId) {
        return leaveRequestMapper.selectByStudentId(studentId);
    }

    @Override
    public List<LeaveRequest> getPendingRequestsForTeacher(Long teacherId) {
        return leaveRequestMapper.selectPendingByTeacherId(teacherId);
    }

    private void syncAttendanceForApprovedLeave(LeaveRequest leaveRequest) {
        if (leaveRequest.getCourseArrangementId() == null) {
            return;
        }

        LocalDate startDate = leaveRequest.getStartTime().toLocalDate();
        LocalDate endDate = leaveRequest.getEndTime().toLocalDate();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            upsertLeaveAttendance(leaveRequest, current);
            current = current.plusDays(1);
        }
    }

    private void upsertLeaveAttendance(LeaveRequest leaveRequest, LocalDate attendanceDate) {
        Attendance existing = attendanceMapper.selectOne(
                new LambdaQueryWrapper<Attendance>()
                        .eq(Attendance::getStudentId, leaveRequest.getStudentId())
                        .eq(Attendance::getCourseArrangementId, leaveRequest.getCourseArrangementId())
                        .eq(Attendance::getAttendanceDate, attendanceDate)
        );

        if (existing == null) {
            Attendance attendance = new Attendance();
            attendance.setStudentId(leaveRequest.getStudentId());
            attendance.setCourseArrangementId(leaveRequest.getCourseArrangementId());
            attendance.setAttendanceDate(attendanceDate);
            attendance.setStatus(Attendance.Status.LEAVE);
            attendance.setRemark("Auto synced from approved leave request #" + leaveRequest.getId());
            attendanceMapper.insert(attendance);
            return;
        }

        // Do not overwrite a confirmed present record.
        if (existing.getStatus() == Attendance.Status.PRESENT) {
            return;
        }
        existing.setStatus(Attendance.Status.LEAVE);
        if (existing.getRemark() == null || existing.getRemark().isBlank()) {
            existing.setRemark("Auto synced from approved leave request #" + leaveRequest.getId());
        }
        attendanceMapper.updateById(existing);
    }
}
