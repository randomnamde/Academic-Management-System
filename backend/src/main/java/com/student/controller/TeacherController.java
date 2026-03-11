package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.dto.TeacherDTO;
import com.student.entity.Teacher;
import com.student.security.CurrentUserService;
import com.student.security.DataScopeService;
import com.student.service.TeacherService;
import com.student.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;
    private final DataScopeService dataScopeService;
    private final CurrentUserService currentUserService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Void> add(@RequestBody @Validated TeacherDTO teacherDTO, Authentication authentication) {
        Long scopedCollegeId = currentUserService.resolveManagedCollegeId(authentication);
        if (scopedCollegeId != null) {
            teacherDTO.setCollegeId(scopedCollegeId);
        }
        teacherService.addTeacher(teacherDTO);
        return ResultVO.success();
    }

    @PutMapping("/{teacherNo}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Void> update(@PathVariable String teacherNo, @RequestBody @Validated TeacherDTO teacherDTO, Authentication authentication) {
        Long scopedCollegeId = currentUserService.resolveManagedCollegeId(authentication);
        if (scopedCollegeId != null) {
            Teacher existing = teacherService.getById(teacherNo);
            if (existing == null || !scopedCollegeId.equals(existing.getCollegeId())) {
                return ResultVO.error(403, "Forbidden");
            }
            teacherDTO.setCollegeId(scopedCollegeId);
        }
        teacherDTO.setTeacherNo(teacherNo);
        teacherService.updateTeacher(teacherDTO);
        return ResultVO.success();
    }

    @DeleteMapping("/{teacherNo}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Void> delete(@PathVariable String teacherNo, Authentication authentication) {
        Long scopedCollegeId = currentUserService.resolveManagedCollegeId(authentication);
        if (scopedCollegeId != null) {
            Teacher existing = teacherService.getById(teacherNo);
            if (existing == null || !scopedCollegeId.equals(existing.getCollegeId())) {
                return ResultVO.error(403, "Forbidden");
            }
        }
        teacherService.deleteTeacher(teacherNo);
        return ResultVO.success();
    }

    @GetMapping("/{teacherNo}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<Teacher> getById(@PathVariable String teacherNo, Authentication authentication) {
        Long scopedCollegeId = currentUserService.resolveManagedCollegeId(authentication);
        if (scopedCollegeId != null) {
            Teacher existing = teacherService.getById(teacherNo);
            if (existing == null || !scopedCollegeId.equals(existing.getCollegeId())) {
                return ResultVO.error(403, "Forbidden");
            }
        }
        Teacher teacher = teacherService.getById(teacherNo);
        if (dataScopeService.isStudent(authentication)) {
            DataScopeService.StudentArrangementScope scope = dataScopeService.resolveStudentArrangementScope(authentication);
            if (teacher == null || teacher.getId() == null || !scope.getTeacherIds().contains(teacher.getId())) {
                return ResultVO.error(403, "Forbidden");
            }
        }
        return ResultVO.success(teacher);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<Page<Teacher>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String teacherNo,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long collegeId,
            @RequestParam(required = false) Long departmentId,
            Authentication authentication) {
        Long scopedCollegeId = currentUserService.resolveManagedCollegeId(authentication);
        Long effectiveCollegeId = scopedCollegeId != null ? scopedCollegeId : collegeId;
        if (scopedCollegeId == null && currentUserService.isTeacher(authentication)) {
            effectiveCollegeId = currentUserService.resolveCurrentCollegeId(authentication);
        }
        if (scopedCollegeId != null) {
            Page<Teacher> pageParam = new Page<>(page, size);
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Teacher> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Teacher>()
                    .eq(Teacher::getCollegeId, effectiveCollegeId)
                    .like(teacherNo != null && !teacherNo.isBlank(), Teacher::getTeacherNo, teacherNo)
                    .like(name != null && !name.isBlank(), Teacher::getName, name);
            Page<Teacher> result = teacherService.page(pageParam, wrapper);
            return ResultVO.success(result);
        }
        if (dataScopeService.isStudent(authentication)) {
            DataScopeService.StudentArrangementScope scope = dataScopeService.resolveStudentArrangementScope(authentication);
            Page<Teacher> pageParam = new Page<>(page, size);
            if (scope.getTeacherIds().isEmpty()) {
                pageParam.setRecords(java.util.Collections.emptyList());
                pageParam.setTotal(0);
                return ResultVO.success(pageParam);
            }
            LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<Teacher>()
                    .in(Teacher::getId, scope.getTeacherIds())
                    .like(teacherNo != null && !teacherNo.isBlank(), Teacher::getTeacherNo, teacherNo)
                    .like(name != null && !name.isBlank(), Teacher::getName, name);
            Page<Teacher> result = teacherService.page(pageParam, wrapper);
            return ResultVO.success(result);
        }
        if (effectiveCollegeId != null) {
            Page<Teacher> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<Teacher>()
                    .eq(Teacher::getCollegeId, effectiveCollegeId)
                    .like(teacherNo != null && !teacherNo.isBlank(), Teacher::getTeacherNo, teacherNo)
                    .like(name != null && !name.isBlank(), Teacher::getName, name);
            Page<Teacher> result = teacherService.page(pageParam, wrapper);
            return ResultVO.success(result);
        }
        Page<Teacher> result = teacherService.getTeacherPage(page, size, teacherNo, name, departmentId);
        return ResultVO.success(result);
    }

    @PutMapping("/{teacherNo}/status")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Void> updateStatus(@PathVariable String teacherNo, @RequestParam Integer status, Authentication authentication) {
        Long scopedCollegeId = currentUserService.resolveManagedCollegeId(authentication);
        if (scopedCollegeId != null) {
            Teacher existing = teacherService.getById(teacherNo);
            if (existing == null || !scopedCollegeId.equals(existing.getCollegeId())) {
                return ResultVO.error(403, "Forbidden");
            }
        }
        teacherService.updateTeacherStatus(teacherNo, status);
        return ResultVO.success();
    }
}

