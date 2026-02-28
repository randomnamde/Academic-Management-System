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
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<Void> add(@RequestBody @Validated CourseDTO courseDTO) {
        courseService.addCourse(courseDTO);
        return ResultVO.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<Void> update(@PathVariable Long id, @RequestBody @Validated CourseDTO courseDTO) {
        courseDTO.setId(id);
        courseService.updateCourse(courseDTO);
        return ResultVO.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultVO<Void> delete(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResultVO.success();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<Course> getById(@PathVariable Long id, Authentication authentication) {
        if (dataScopeService.isStudent(authentication)) {
            DataScopeService.StudentArrangementScope scope = dataScopeService.resolveStudentArrangementScope(authentication);
            if (!scope.getCourseIds().contains(id)) {
                return ResultVO.error(403, "Forbidden");
            }
        }
        Course course = courseService.getCourseById(id);
        return ResultVO.success(course);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
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
            if (scope.getCourseIds().isEmpty()) {
                pageParam.setRecords(Collections.emptyList());
                pageParam.setTotal(0);
                return ResultVO.success(pageParam);
            }
            LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                    .in(Course::getId, scope.getCourseIds())
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
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<CourseCategoryStatisticsDTO> getCategoryStatistics(Authentication authentication) {
        if (dataScopeService.isStudent(authentication)) {
            DataScopeService.StudentArrangementScope scope = dataScopeService.resolveStudentArrangementScope(authentication);
            CourseCategoryStatisticsDTO statistics = new CourseCategoryStatisticsDTO();
            if (scope.getCourseIds().isEmpty()) {
                return ResultVO.success(statistics);
            }
            List<Course> courses = courseService.list(new LambdaQueryWrapper<Course>().in(Course::getId, scope.getCourseIds()));
            statistics.setRequired(courses.stream().filter(item -> item.getCategory() == Course.Category.REQUIRED).count());
            statistics.setElective(courses.stream().filter(item -> item.getCategory() == Course.Category.ELECTIVE).count());
            statistics.setPractical(courses.stream().filter(item -> item.getCategory() == Course.Category.PRACTICAL).count());
            return ResultVO.success(statistics);
        }
        return ResultVO.success(courseService.getCategoryStatistics());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        courseService.updateCourseStatus(id, status);
        return ResultVO.success();
    }
}
