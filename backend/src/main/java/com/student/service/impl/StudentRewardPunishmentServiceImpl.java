package com.student.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.StudentRewardPunishmentDTO;
import com.student.entity.StudentRewardPunishment;
import com.student.exception.BusinessException;
import com.student.mapper.StudentRewardPunishmentMapper;
import com.student.service.StudentRewardPunishmentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudentRewardPunishmentServiceImpl implements StudentRewardPunishmentService {

    @Autowired
    private StudentRewardPunishmentMapper recordMapper;

    @Override
    public Page<StudentRewardPunishment> getRecordPage(Integer page, Integer size, String studentId,
                                                        String type, String status, String category) {
        int offset = (page - 1) * size;
        List<StudentRewardPunishment> records = recordMapper.selectPageWithDetail(offset, size, studentId,
                type, status, category);
        Long total = recordMapper.countWithDetail(studentId, type, status, category);

        Page<StudentRewardPunishment> result = new Page<>(page, size);
        result.setRecords(records);
        result.setTotal(total);
        return result;
    }

    @Override
    @Transactional
    public StudentRewardPunishment createRecord(StudentRewardPunishmentDTO dto) {
        StudentRewardPunishment record = new StudentRewardPunishment();
        BeanUtils.copyProperties(dto, record);
        record.setType(StudentRewardPunishment.Type.valueOf(dto.getType()));
        record.setStatus(StudentRewardPunishment.Status.PENDING);
        recordMapper.insert(record);
        return record;
    }

    @Override
    @Transactional
    public StudentRewardPunishment updateRecord(Long id, StudentRewardPunishmentDTO dto) {
        StudentRewardPunishment existing = recordMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "记录不存在");
        }

        BeanUtils.copyProperties(dto, existing, "id", "createTime", "status", "approverNo", "approveTime");
        existing.setType(StudentRewardPunishment.Type.valueOf(dto.getType()));
        recordMapper.updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public void deleteRecord(Long id) {
        recordMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void approveRecord(Long id, String approverNo) {
        StudentRewardPunishment record = recordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(404, "记录不存在");
        }
        record.setStatus(StudentRewardPunishment.Status.APPROVED);
        record.setApproverNo(approverNo);
        record.setApproveTime(LocalDateTime.now());
        recordMapper.updateById(record);
    }

    @Override
    @Transactional
    public void rejectRecord(Long id, String approverNo, String reason) {
        StudentRewardPunishment record = recordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(404, "记录不存在");
        }
        record.setStatus(StudentRewardPunishment.Status.REJECTED);
        record.setApproverNo(approverNo);
        record.setApproveTime(LocalDateTime.now());
        if (reason != null) {
            record.setReason(record.getReason() + "\n驳回原因: " + reason);
        }
        recordMapper.updateById(record);
    }
}
