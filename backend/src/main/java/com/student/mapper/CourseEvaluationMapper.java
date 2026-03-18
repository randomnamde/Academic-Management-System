package com.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.entity.CourseEvaluation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface CourseEvaluationMapper extends BaseMapper<CourseEvaluation> {

    List<CourseEvaluation> selectPageWithDetail(@Param("page") Integer page, @Param("size") Integer size,
                                                  @Param("studentId") String studentId,
                                                  @Param("courseArrangementId") Long courseArrangementId,
                                                  @Param("teacherNo") String teacherNo,
                                                  @Param("status") String status);

    Long countWithDetail(@Param("studentId") String studentId,
                          @Param("courseArrangementId") Long courseArrangementId,
                          @Param("teacherNo") String teacherNo,
                          @Param("status") String status);

    List<Map<String, Object>> selectTeacherStatistics(@Param("teacherNo") String teacherNo,
                                                        @Param("semester") String semester);

    List<Map<String, Object>> selectCourseStatistics(@Param("courseArrangementId") Long courseArrangementId);
}
