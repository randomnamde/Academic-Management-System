package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.dto.CourseCategoryStatisticsDTO;
import com.student.dto.CourseDTO;
import com.student.entity.Course;
import com.student.security.DataScopeService;
import com.student.service.CourseService;
import com.student.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final DataScopeService dataScopeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<Void> add(@RequestBody @Validated CourseDTO courseDTO) {
        courseService.addCourse(courseDTO);
        return ResultVO.success();
    }

    @PutMapping("/{courseCode}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<Void> update(@PathVariable String courseCode, @RequestBody @Validated CourseDTO courseDTO) {
        courseDTO.setCourseCode(courseCode);
        courseService.updateCourse(courseDTO);
        return ResultVO.success();
    }

    @DeleteMapping("/{courseCode}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Void> delete(@PathVariable String courseCode) {
        courseService.deleteCourse(courseCode);
        return ResultVO.success();
    }

    @GetMapping("/{courseCode}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<Course> getById(@PathVariable String courseCode, Authentication authentication) {
        if (dataScopeService.isStudent(authentication)) {
            DataScopeService.StudentArrangementScope scope = dataScopeService.resolveStudentArrangementScope(authentication);
            if (!scope.getCourseCodes().contains(courseCode)) {
                return ResultVO.error(403, "Forbidden");
            }
        }
        Course course = courseService.getCourseById(courseCode);
        return ResultVO.success(course);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<Page<Course>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String courseCode,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) Course.Category category,
            Authentication authentication) {
        if (dataScopeService.isStudent(authentication)) {
            DataScopeService.StudentArrangementScope scope = dataScopeService.resolveStudentArrangementScope(authentication);
            Page<Course> pageParam = new Page<>(page, size);
            if (scope.getCourseCodes().isEmpty()) {
                pageParam.setRecords(Collections.emptyList());
                pageParam.setTotal(0);
                return ResultVO.success(pageParam);
            }
            LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                    .in(Course::getCourseCode, scope.getCourseCodes())
                    .like(courseCode != null && !courseCode.isBlank(), Course::getCourseCode, courseCode)
                    .like(courseName != null && !courseName.isBlank(), Course::getCourseName, courseName)
                    .eq(category != null, Course::getCategory, category);
            Page<Course> result = courseService.page(pageParam, wrapper);
            return ResultVO.success(result);
        }
        Page<Course> result = courseService.getCoursePage(page, size, courseCode, courseName, category);
        return ResultVO.success(result);
    }

    @GetMapping("/statistics/category")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<CourseCategoryStatisticsDTO> getCategoryStatistics(Authentication authentication) {
        if (dataScopeService.isStudent(authentication)) {
            DataScopeService.StudentArrangementScope scope = dataScopeService.resolveStudentArrangementScope(authentication);
            CourseCategoryStatisticsDTO statistics = new CourseCategoryStatisticsDTO();
            if (scope.getCourseCodes().isEmpty()) {
                return ResultVO.success(statistics);
            }
            List<Course> courses = courseService.list(new LambdaQueryWrapper<Course>().in(Course::getCourseCode, scope.getCourseCodes()));
            statistics.setRequired(courses.stream().filter(item -> item.getCategory() == Course.Category.REQUIRED).count());
            statistics.setElective(courses.stream().filter(item -> item.getCategory() == Course.Category.ELECTIVE).count());
            statistics.setPractical(courses.stream().filter(item -> item.getCategory() == Course.Category.PRACTICAL).count());
            return ResultVO.success(statistics);
        }
        return ResultVO.success(courseService.getCategoryStatistics());
    }

    @PutMapping("/{courseCode}/status")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<Void> updateStatus(@PathVariable String courseCode, @RequestParam Integer status) {
        courseService.updateCourseStatus(courseCode, status);
        return ResultVO.success();
    }
}



