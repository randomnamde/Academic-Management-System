package com.student.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.ExamArrangementDTO;
import com.student.entity.ExamArrangement;
import com.student.exception.BusinessException;
import com.student.mapper.ExamArrangementMapper;
import com.student.service.ExamArrangementService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ExamArrangementServiceImpl implements ExamArrangementService {

    @Autowired
    private ExamArrangementMapper examMapper;

    @Override
    public Page<ExamArrangement> getExamPage(Integer page, Integer size, Long courseArrangementId,
                                              String examType, String status, String examDate) {
        int offset = (page - 1) * size;
        List<ExamArrangement> records = examMapper.selectPageWithDetail(offset, size, courseArrangementId,
                examType, status, examDate);
        Long total = examMapper.countWithDetail(courseArrangementId, examType, status, examDate);

        Page<ExamArrangement> result = new Page<>(page, size);
        result.setRecords(records);
        result.setTotal(total);
        return result;
    }

    @Override
    @Transactional
    public ExamArrangement createExam(ExamArrangementDTO dto) {
        ExamArrangement exam = new ExamArrangement();
        BeanUtils.copyProperties(dto, exam);
        exam.setExamType(ExamArrangement.ExamType.valueOf(dto.getExamType()));
        exam.setStatus(ExamArrangement.Status.SCHEDULED);
        if (dto.getCapacity() == null) {
            exam.setCapacity(50);
        }
        exam.setEnrolledCount(0);
        examMapper.insert(exam);
        return exam;
    }

    @Override
    @Transactional
    public ExamArrangement updateExam(Long id, ExamArrangementDTO dto) {
        ExamArrangement existing = examMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "考试安排不存在");
        }

        BeanUtils.copyProperties(dto, existing, "id", "createTime", "enrolledCount");
        existing.setExamType(ExamArrangement.ExamType.valueOf(dto.getExamType()));
        examMapper.updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public void deleteExam(Long id) {
        examMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        ExamArrangement exam = examMapper.selectById(id);
        if (exam == null) {
            throw new BusinessException(404, "考试安排不存在");
        }
        exam.setStatus(ExamArrangement.Status.valueOf(status));
        examMapper.updateById(exam);
    }
}
