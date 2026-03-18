package com.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.dto.CourseCategoryStatisticsDTO;
import com.student.dto.CourseDTO;
import com.student.entity.Course;

public interface CourseService extends IService<Course> {

    void addCourse(CourseDTO courseDTO);

    void updateCourse(CourseDTO courseDTO);

    void deleteCourse(String courseCode);

    Course getCourseById(String courseCode);

    Page<Course> getCoursePage(Integer page, Integer size, String courseCode, String courseName, Course.Category category);

    void updateCourseStatus(String courseCode, Integer status);

    CourseCategoryStatisticsDTO getCategoryStatistics();
}
