package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.entity.Class;
import com.student.security.DataScopeService;
import com.student.service.ClassService;
import com.student.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/class")
@RequiredArgsConstructor
public class ClassController {

    private final ClassService classService;
    private final DataScopeService dataScopeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<Void> add(@RequestBody @Validated Class clazz) {
        classService.save(clazz);
        return ResultVO.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<Void> update(@PathVariable Long id, @RequestBody @Validated Class clazz) {
        clazz.setId(id);
        classService.updateById(clazz);
        return ResultVO.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultVO<Void> delete(@PathVariable Long id) {
        classService.removeById(id);
        return ResultVO.success();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<Class> getById(@PathVariable Long id, Authentication authentication) {
        if (dataScopeService.isStudent(authentication)) {
            DataScopeService.StudentArrangementScope scope = dataScopeService.resolveStudentArrangementScope(authentication);
            if (!scope.getClassIds().contains(id)) {
                return ResultVO.error(403, "Forbidden");
            }
        }
        Class clazz = classService.getClassById(id);
        return ResultVO.success(clazz);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<Page<Class>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) Long teacherId,
            Authentication authentication) {
        if (dataScopeService.isStudent(authentication)) {
            DataScopeService.StudentArrangementScope scope = dataScopeService.resolveStudentArrangementScope(authentication);
            Page<Class> pageParam = new Page<>(page, size);
            if (scope.getClassIds().isEmpty()) {
                pageParam.setRecords(Collections.emptyList());
                pageParam.setTotal(0);
                return ResultVO.success(pageParam);
            }

            Year gradeYear = null;
            if (grade != null && !grade.isBlank()) {
                try {
                    gradeYear = Year.parse(grade);
                } catch (Exception ignored) {
                    gradeYear = null;
                }
            }

            LambdaQueryWrapper<Class> wrapper = new LambdaQueryWrapper<Class>()
                    .in(Class::getId, scope.getClassIds())
                    .like(className != null && !className.isBlank(), Class::getClassName, className)
                    .eq(gradeYear != null, Class::getGrade, gradeYear)
                    .eq(teacherId != null, Class::getTeacherId, teacherId);
            Page<Class> result = classService.page(pageParam, wrapper);
            return ResultVO.success(result);
        }
        Page<Class> result = classService.getClassPage(page, size, className, grade, teacherId);
        return ResultVO.success(result);
    }

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<List<Class>> getByTeacherId(@PathVariable Long teacherId) {
        List<Class> classes = classService.getClassesByTeacherId(teacherId);
        return ResultVO.success(classes);
    }
}
