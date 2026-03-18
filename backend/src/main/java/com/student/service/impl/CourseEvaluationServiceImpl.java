package com.student.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.CourseEvaluationDTO;
import com.student.entity.CourseEvaluation;
import com.student.exception.BusinessException;
import com.student.mapper.CourseEvaluationMapper;
import com.student.service.CourseEvaluationService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
public class CourseEvaluationServiceImpl implements CourseEvaluationService {

    @Autowired
    private CourseEvaluationMapper evaluationMapper;

    @Override
    public Page<CourseEvaluation> getEvaluationPage(Integer page, Integer size, String studentId,
                                                   Long courseArrangementId, String teacherNo, String status) {
        int offset = (page - 1) * size;
        List<CourseEvaluation> records = evaluationMapper.selectPageWithDetail(offset, size, studentId,
                courseArrangementId, teacherNo, status);
        Long total = evaluationMapper.countWithDetail(studentId, courseArrangementId, teacherNo, status);

        Page<CourseEvaluation> result = new Page<>(page, size);
        result.setRecords(records);
        result.setTotal(total);
        return result;
    }

    @Override
    @Transactional
    public CourseEvaluation createEvaluation(CourseEvaluationDTO dto) {
        CourseEvaluation evaluation = new CourseEvaluation();
        BeanUtils.copyProperties(dto, evaluation);

        // 计算总分
        if (dto.getTeachingScore() != null && dto.getContentScore() != null && dto.getMethodScore() != null) {
            BigDecimal total = dto.getTeachingScore()
                    .add(dto.getContentScore())
                    .add(dto.getMethodScore())
                    .divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP);
            evaluation.setOverallScore(total);
        }

        evaluation.setStatus(CourseEvaluation.Status.PENDING);
        evaluationMapper.insert(evaluation);
        return evaluation;
    }

    @Override
    @Transactional
    public CourseEvaluation updateEvaluation(Long id, CourseEvaluationDTO dto) {
        CourseEvaluation existing = evaluationMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "评价不存在");
        }

        BeanUtils.copyProperties(dto, existing, "id", "createTime");

        // 重新计算总分
        if (dto.getTeachingScore() != null && dto.getContentScore() != null && dto.getMethodScore() != null) {
            BigDecimal total = dto.getTeachingScore()
                    .add(dto.getContentScore())
                    .add(dto.getMethodScore())
                    .divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP);
            existing.setOverallScore(total);
        }

        evaluationMapper.updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public void deleteEvaluation(Long id) {
        evaluationMapper.deleteById(id);
    }

    @Override
    public Map<String, Object> getTeacherStatistics(String teacherNo, String semester) {
        List<Map<String, Object>> results = evaluationMapper.selectTeacherStatistics(teacherNo, semester);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<Map<String, Object>> getCourseStatistics(Long courseArrangementId) {
        return evaluationMapper.selectCourseStatistics(courseArrangementId);
    }

    @Override
    @Transactional
    public void publishEvaluation(Long id) {
        CourseEvaluation evaluation = evaluationMapper.selectById(id);
        if (evaluation == null) {
            throw new BusinessException(404, "评价不存在");
        }
        evaluation.setStatus(CourseEvaluation.Status.PUBLISHED);
        evaluationMapper.updateById(evaluation);
    }
}
