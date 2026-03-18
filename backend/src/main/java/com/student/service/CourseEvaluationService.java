package com.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.CourseEvaluationDTO;
import com.student.entity.CourseEvaluation;
import java.util.List;
import java.util.Map;

public interface CourseEvaluationService {

    Page<CourseEvaluation> getEvaluationPage(Integer page, Integer size, String studentId,
                                              Long courseArrangementId, String teacherNo, String status);

    CourseEvaluation createEvaluation(CourseEvaluationDTO dto);

    CourseEvaluation updateEvaluation(Long id, CourseEvaluationDTO dto);

    void deleteEvaluation(Long id);

    Map<String, Object> getTeacherStatistics(String teacherNo, String semester);

    List<Map<String, Object>> getCourseStatistics(Long courseArrangementId);

    void publishEvaluation(Long id);
}
