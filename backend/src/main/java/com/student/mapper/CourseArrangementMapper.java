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
                                                 @Param("collegeCode") String collegeCode,
                                                 @Param("courseCode") String courseCode,
                                                 @Param("teacherNo") String teacherNo,
                                                 @Param("classId") String classId,
                                                 @Param("semester") String semester,
                                                 @Param("status") Integer status);

    List<CourseArrangement> selectListWithDetail(@Param("teacherNo") String teacherNo,
                                                 @Param("classId") String classId,
                                                 @Param("status") Integer status);

    @org.apache.ibatis.annotations.Update("UPDATE course_arrangement SET enrolled_count = enrolled_count + #{count} WHERE id = #{id} AND enrolled_count + #{count} <= capacity")
    int updateEnrolledCount(@Param("id") Long id, @Param("count") int count);
}


