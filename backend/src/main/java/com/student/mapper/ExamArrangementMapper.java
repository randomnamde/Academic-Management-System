package com.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.entity.ExamArrangement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ExamArrangementMapper extends BaseMapper<ExamArrangement> {

    List<ExamArrangement> selectPageWithDetail(@Param("page") Integer page, @Param("size") Integer size,
                                                @Param("courseArrangementId") Long courseArrangementId,
                                                @Param("examType") String examType,
                                                @Param("status") String status,
                                                @Param("examDate") String examDate);

    Long countWithDetail(@Param("courseArrangementId") Long courseArrangementId,
                          @Param("examType") String examType,
                          @Param("status") String status,
                          @Param("examDate") String examDate);
}
