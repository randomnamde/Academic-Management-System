package com.student.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.dto.CourseCategoryStatisticsDTO;
import com.student.dto.CourseDTO;
import com.student.entity.Course;
import com.student.exception.BusinessException;
import com.student.mapper.CourseMapper;
import com.student.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    private final CourseMapper courseMapper;

    @Override
    @Transactional
    public void addCourse(CourseDTO courseDTO) {
        if (courseMapper.selectByCourseCode(courseDTO.getCourseCode()) != null) {
            throw new BusinessException("Course code already exists");
        }

        Course course = new Course();
        BeanUtils.copyProperties(courseDTO, course);
        courseMapper.insert(course);
    }

    @Override
    @Transactional
    public void updateCourse(CourseDTO courseDTO) {
        if (courseDTO.getId() == null) {
            throw new BusinessException("Course id cannot be null");
        }

        Course existCourse = courseMapper.selectById(courseDTO.getId());
        if (existCourse == null) {
            throw new BusinessException("Course not found");
        }

        Course existByCode = courseMapper.selectByCourseCode(courseDTO.getCourseCode());
        if (existByCode != null && !existByCode.getId().equals(courseDTO.getId())) {
            throw new BusinessException("Course code is already used by another course");
        }

        Course course = new Course();
        BeanUtils.copyProperties(courseDTO, course);
        courseMapper.updateById(course);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException("Course not found");
        }

        courseMapper.deleteById(id);
    }

    @Override
    public Course getCourseById(Long id) {
        return courseMapper.selectById(id);
    }

    @Override
    public Page<Course> getCoursePage(Integer page, Integer size, String courseCode, String courseName, Course.Category category) {
        Page<Course> pageParam = new Page<>(page, size);
        return courseMapper.selectPageList(pageParam, courseCode, courseName, category);
    }

    @Override
    @Transactional
    public void updateCourseStatus(Long id, Integer status) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException("Course not found");
        }

        course.setStatus(status);
        courseMapper.updateById(course);
    }

    @Override
    public CourseCategoryStatisticsDTO getCategoryStatistics() {
        CourseCategoryStatisticsDTO statistics = new CourseCategoryStatisticsDTO();
        statistics.setRequired(lambdaQuery().eq(Course::getCategory, Course.Category.REQUIRED).count());
        statistics.setElective(lambdaQuery().eq(Course::getCategory, Course.Category.ELECTIVE).count());
        statistics.setPractical(lambdaQuery().eq(Course::getCategory, Course.Category.PRACTICAL).count());
        return statistics;
    }
}
