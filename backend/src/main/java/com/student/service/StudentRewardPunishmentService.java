package com.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.StudentRewardPunishmentDTO;
import com.student.entity.StudentRewardPunishment;

public interface StudentRewardPunishmentService {

    Page<StudentRewardPunishment> getRecordPage(Integer page, Integer size, String studentId,
                                                  String type, String status, String category);

    StudentRewardPunishment createRecord(StudentRewardPunishmentDTO dto);

    StudentRewardPunishment updateRecord(Long id, StudentRewardPunishmentDTO dto);

    void deleteRecord(Long id);

    void approveRecord(Long id, String approverNo);

    void rejectRecord(Long id, String approverNo, String reason);
}
