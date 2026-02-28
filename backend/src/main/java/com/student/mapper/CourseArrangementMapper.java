package com.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.entity.CourseArrangement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseArrangementMapper extends BaseMapper<CourseArrangement> {

    CourseArrangement selectByIdWithDetail(@Param("id") Long id);

    Page<CourseArrangement> selectPageWithDetail(Page<CourseArrangement> page,
                                                 @Param("courseId") Long courseId,
                                                 @Param("teacherId") Long teacherId,
                                                 @Param("classId") Long classId,
                                                 @Param("semester") String semester,
                                                 @Param("status") Integer status);

    List<CourseArrangement> selectListWithDetail(@Param("teacherId") Long teacherId,
                                                 @Param("classId") Long classId,
                                                 @Param("status") Integer status);
}
