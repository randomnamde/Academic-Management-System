package com.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.ExamArrangementDTO;
import com.student.entity.ExamArrangement;

public interface ExamArrangementService {

    Page<ExamArrangement> getExamPage(Integer page, Integer size, Long courseArrangementId,
                                       String examType, String status, String examDate);

    ExamArrangement createExam(ExamArrangementDTO dto);

    ExamArrangement updateExam(Long id, ExamArrangementDTO dto);

    void deleteExam(Long id);

    void updateStatus(Long id, String status);
}
