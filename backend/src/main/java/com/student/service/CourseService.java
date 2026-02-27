package com.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.dto.CourseDTO;
import com.student.entity.Course;

public interface CourseService extends IService<Course> {

    void addCourse(CourseDTO courseDTO);

    void updateCourse(CourseDTO courseDTO);

    void deleteCourse(Long id);

    Course getCourseById(Long id);

    Page<Course> getCoursePage(Integer page, Integer size, String courseCode, String courseName, Course.Category category);

    void updateCourseStatus(Long id, Integer status);
}
