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
                                     @Param("studentId") Long studentId,
                                     @Param("courseArrangementId") Long courseArrangementId,
                                     @Param("semester") String semester);
    
    @Select("SELECT * FROM score WHERE student_id = #{studentId} AND course_arrangement_id = #{courseArrangementId}")
    Score selectByStudentAndCourse(@Param("studentId") Long studentId, 
                                    @Param("courseArrangementId") Long courseArrangementId);
    
    List<Score> selectByStudentId(@Param("studentId") Long studentId);
    
    List<Score> selectByCourseArrangementId(@Param("courseArrangementId") Long courseArrangementId);
    
    @Select("SELECT AVG(total_score) FROM score WHERE student_id = #{studentId}")
    Double selectAverageScoreByStudent(@Param("studentId") Long studentId);
    
    @Select("SELECT COUNT(*) FROM score WHERE student_id = #{studentId} AND total_score >= 60")
    Long countPassedByStudent(@Param("studentId") Long studentId);
    
    @Select("SELECT COUNT(*) FROM score WHERE student_id = #{studentId}")
    Long countTotalByStudent(@Param("studentId") Long studentId);
    
    List<Map<String, Object>> selectScoreDistribution(@Param("courseArrangementId") Long courseArrangementId);
    
    List<Map<String, Object>> selectClassRank(@Param("classId") Long classId, @Param("semester") String semester);
}
