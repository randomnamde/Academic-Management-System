package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.entity.Class;
import com.student.entity.Major;
import com.student.entity.Teacher;
import com.student.mapper.MajorMapper;
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
    private final MajorMapper majorMapper;
    private final SysUserService sysUserService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<Void> add(@RequestBody @Validated Class clazz, Authentication authentication) {
        bindCollegeFromMajor(authentication, clazz);
        classService.createClass(clazz);
        ensureHomeroomRole(clazz.getTeacherId());
        return ResultVO.success();
    }

    @PutMapping("/{classCode}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<Void> update(@PathVariable String classCode, @RequestBody @Validated Class clazz, Authentication authentication) {
        Long scopedCollegeId = currentUserService.resolveManagedCollegeId(authentication);
        bindCollegeFromMajor(authentication, clazz);
        Class existing = classService.getClassByCode(classCode);
        if (existing == null) {
            return ResultVO.error(404, "Class not found");
        }
        if (scopedCollegeId != null) {
            if (!scopedCollegeId.equals(existing.getCollegeId())) {
                return ResultVO.error(403, "Forbidden");
            }
        }
        classService.updateClassByCode(classCode, clazz);
        ensureHomeroomRole(clazz.getTeacherId());
        return ResultVO.success();
    }

    @DeleteMapping("/{classCode}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Void> delete(@PathVariable String classCode, Authentication authentication) {
        Long scopedCollegeId = currentUserService.resolveManagedCollegeId(authentication);
        Class existing = classService.getClassByCode(classCode);
        if (existing == null) {
            return ResultVO.error(404, "Class not found");
        }
        if (scopedCollegeId != null) {
            if (!scopedCollegeId.equals(existing.getCollegeId())) {
                return ResultVO.error(403, "Forbidden");
            }
        }
        classService.deleteClassByCode(classCode);
        return ResultVO.success();
    }

    @GetMapping("/{classCode}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<Class> getById(@PathVariable String classCode, Authentication authentication) {
        Long scopedCollegeId = currentUserService.resolveManagedCollegeId(authentication);
        Class existing = classService.getClassByCode(classCode);
        if (existing == null) {
            return ResultVO.error(404, "Class not found");
        }
        if (scopedCollegeId != null) {
            if (!scopedCollegeId.equals(existing.getCollegeId())) {
                return ResultVO.error(403, "Forbidden");
            }
        }
        if (dataScopeService.isStudent(authentication)) {
            DataScopeService.StudentArrangementScope scope = dataScopeService.resolveStudentArrangementScope(authentication);
            if (!scope.getClassIds().contains(existing.getClassCode())) {
                return ResultVO.error(403, "Forbidden");
            }
        }
        return ResultVO.success(existing);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<Page<Class>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) Long collegeId,
            @RequestParam(required = false) String majorCode,
            @RequestParam(required = false) Long teacherId,
            Authentication authentication) {
        Long scopedCollegeId = currentUserService.resolveManagedCollegeId(authentication);
        Long effectiveCollegeId = scopedCollegeId != null ? scopedCollegeId : collegeId;
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
                    .in(Class::getClassCode, scope.getClassIds())
                    .like(className != null && !className.isBlank(), Class::getClassName, className)
                    .eq(gradeYear != null, Class::getGrade, gradeYear)
                    .eq(majorCode != null && !majorCode.isBlank(), Class::getMajorCode, majorCode)
                    .eq(teacherId != null, Class::getTeacherId, teacherId);
            Page<Class> result = classService.page(pageParam, wrapper);
            return ResultVO.success(result);
        }
        Page<Class> result = classService.getClassPage(page, size, className, grade, teacherId, effectiveCollegeId, majorCode);
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
        Teacher teacher = teacherMapper.selectByInternalId(teacherId);
        if (teacher == null || teacher.getUserId() == null) {
            return;
        }
        sysUserService.grantRole(teacher.getUserId(), RoleCode.HOMEROOM_TEACHER);
    }

    private void bindCollegeFromMajor(Authentication authentication, Class clazz) {
        if (clazz == null || clazz.getMajorCode() == null || clazz.getMajorCode().isBlank()) {
            throw new com.student.exception.BusinessException(400, "Major is required");
        }
        Major major = majorMapper.selectById(clazz.getMajorCode());
        if (major == null) {
            throw new com.student.exception.BusinessException(404, "Major not found");
        }
        Long scopedCollegeId = currentUserService.resolveManagedCollegeId(authentication);
        if (scopedCollegeId != null && !scopedCollegeId.equals(major.getCollegeId())) {
            throw new com.student.exception.BusinessException(403, "Forbidden");
        }
        clazz.setCollegeId(major.getCollegeId());
    }
}

