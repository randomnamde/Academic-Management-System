package com.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
    
    @Select("SELECT * FROM course WHERE course_code = #{courseCode}")
    Course selectByCourseCode(@Param("courseCode") String courseCode);
    
    Page<Course> selectPageList(Page<Course> page,
                               @Param("courseCode") String courseCode,
                               @Param("courseName") String courseName,
                               @Param("category") Course.Category category);
}
