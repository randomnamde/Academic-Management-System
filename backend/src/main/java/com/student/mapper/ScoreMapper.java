package com.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.entity.Score;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ScoreMapper extends BaseMapper<Score> {
    
    Score selectByIdWithDetail(@Param("id") Long id);
    
    Page<Score> selectPageWithDetail(Page<Score> page,
                                     @Param("studentId") String studentId,
                                     @Param("teacherNo") String teacherNo,
                                     @Param("courseArrangementId") Long courseArrangementId,
                                     @Param("semester") String semester,
                                     @Param("collegeCode") String collegeCode,
                                     @Param("classId") String classId);
    
    @Select("SELECT * FROM score WHERE student_id = #{studentId} AND course_arrangement_id = #{courseArrangementId}")
    Score selectByStudentAndCourse(@Param("studentId") String studentId, 
                                    @Param("courseArrangementId") Long courseArrangementId);
    
    List<Score> selectByStudentId(@Param("studentId") String studentId);
    
    List<Score> selectByCourseArrangementId(@Param("courseArrangementId") Long courseArrangementId);
    
    @Select("SELECT AVG(total_score) FROM score WHERE student_id = #{studentId}")
    Double selectAverageScoreByStudent(@Param("studentId") String studentId);
    
    @Select("SELECT COUNT(*) FROM score WHERE student_id = #{studentId} AND total_score >= 60")
    Long countPassedByStudent(@Param("studentId") String studentId);
    
    @Select("SELECT COUNT(*) FROM score WHERE student_id = #{studentId}")
    Long countTotalByStudent(@Param("studentId") String studentId);
    
    List<Map<String, Object>> selectScoreDistribution(@Param("courseArrangementId") Long courseArrangementId);
    
    List<Map<String, Object>> selectClassRank(@Param("classCode") String classCode, @Param("semester") String semester);
}


