package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.entity.Class;
import com.student.entity.Teacher;
import com.student.security.CurrentUserService;
import com.student.security.DataScopeService;
import com.student.security.RoleCode;
import com.student.service.SysUserService;
import com.student.service.ClassService;
import com.student.mapper.TeacherMapper;
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
    private final CurrentUserService currentUserService;
    private final TeacherMapper teacherMapper;
    private final SysUserService sysUserService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<Void> add(@RequestBody @Validated Class clazz, Authentication authentication) {
        Long scopedCollegeId = currentUserService.resolveManagedCollegeId(authentication);
        if (scopedCollegeId != null) {
            clazz.setCollegeId(scopedCollegeId);
        }
        classService.save(clazz);
        ensureHomeroomRole(clazz.getTeacherId());
        return ResultVO.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<Void> update(@PathVariable Long id, @RequestBody @Validated Class clazz, Authentication authentication) {
        Long scopedCollegeId = currentUserService.resolveManagedCollegeId(authentication);
        if (scopedCollegeId != null) {
            Class existing = classService.getById(id);
            if (existing == null || !scopedCollegeId.equals(existing.getCollegeId())) {
                return ResultVO.error(403, "Forbidden");
            }
            clazz.setCollegeId(scopedCollegeId);
        }
        clazz.setId(id);
        classService.updateById(clazz);
        ensureHomeroomRole(clazz.getTeacherId());
        return ResultVO.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Void> delete(@PathVariable Long id, Authentication authentication) {
        Long scopedCollegeId = currentUserService.resolveManagedCollegeId(authentication);
        if (scopedCollegeId != null) {
            Class existing = classService.getById(id);
            if (existing == null || !scopedCollegeId.equals(existing.getCollegeId())) {
                return ResultVO.error(403, "Forbidden");
            }
        }
        classService.removeById(id);
        return ResultVO.success();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<Class> getById(@PathVariable Long id, Authentication authentication) {
        Long scopedCollegeId = currentUserService.resolveManagedCollegeId(authentication);
        if (scopedCollegeId != null) {
            Class existing = classService.getById(id);
            if (existing == null || !scopedCollegeId.equals(existing.getCollegeId())) {
                return ResultVO.error(403, "Forbidden");
            }
        }
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
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<Page<Class>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) Long collegeId,
            @RequestParam(required = false) Long teacherId,
            Authentication authentication) {
        Long scopedCollegeId = currentUserService.resolveManagedCollegeId(authentication);
        Long effectiveCollegeId = scopedCollegeId != null ? scopedCollegeId : collegeId;
        if (scopedCollegeId != null) {
            Page<Class> pageParam = new Page<>(page, size);
            Year gradeYear = null;
            if (grade != null && !grade.isBlank()) {
                try {
                    gradeYear = Year.parse(grade);
                } catch (Exception ignored) {
                    gradeYear = null;
                }
            }
            LambdaQueryWrapper<Class> wrapper = new LambdaQueryWrapper<Class>()
                    .eq(Class::getCollegeId, effectiveCollegeId)
                    .like(className != null && !className.isBlank(), Class::getClassName, className)
                    .eq(gradeYear != null, Class::getGrade, gradeYear)
                    .eq(teacherId != null, Class::getTeacherId, teacherId);
            Page<Class> result = classService.page(pageParam, wrapper);
            return ResultVO.success(result);
        }
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
        if (effectiveCollegeId != null) {
            Page<Class> pageParam = new Page<>(page, size);
            Year gradeYear = null;
            if (grade != null && !grade.isBlank()) {
                try {
                    gradeYear = Year.parse(grade);
                } catch (Exception ignored) {
                    gradeYear = null;
                }
            }
            LambdaQueryWrapper<Class> wrapper = new LambdaQueryWrapper<Class>()
                    .eq(Class::getCollegeId, effectiveCollegeId)
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
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<List<Class>> getByTeacherId(@PathVariable Long teacherId, Authentication authentication) {
        List<Class> classes = classService.getClassesByTeacherId(teacherId);
        Long scopedCollegeId = currentUserService.resolveManagedCollegeId(authentication);
        if (scopedCollegeId != null) {
            classes = classes.stream()
                    .filter(item -> scopedCollegeId.equals(item.getCollegeId()))
                    .toList();
        }
        return ResultVO.success(classes);
    }

    private void ensureHomeroomRole(Long teacherId) {
        if (teacherId == null) {
            return;
        }
        Teacher teacher = teacherMapper.selectById(teacherId);
        if (teacher == null || teacher.getUserId() == null) {
            return;
        }
        sysUserService.grantRole(teacher.getUserId(), RoleCode.HOMEROOM_TEACHER);
    }
}

