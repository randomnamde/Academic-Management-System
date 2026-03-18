package com.student.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
        String scopedCollegeCode = currentUserService.resolveManagedCollegeCode(authentication);
        if (scopedCollegeCode != null) {
            teacherDTO.setCollegeCode(scopedCollegeCode);
        }
        teacherService.addTeacher(teacherDTO);
        return ResultVO.success();
    }

    @GetMapping("/next-no")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<String> nextTeacherNo(@RequestParam String collegeCode, Authentication authentication) {
        String scopedCollegeCode = currentUserService.resolveManagedCollegeCode(authentication);
        String effectiveCollegeCode = scopedCollegeCode != null ? scopedCollegeCode : collegeCode;
        return ResultVO.success(teacherService.generateNextTeacherNo(effectiveCollegeCode));
    }

    @PutMapping("/{teacherNo}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Void> update(@PathVariable String teacherNo, @RequestBody @Validated TeacherDTO teacherDTO, Authentication authentication) {
        String scopedCollegeCode = currentUserService.resolveManagedCollegeCode(authentication);
        if (scopedCollegeCode != null) {
            Teacher existing = teacherService.getById(teacherNo);
            if (existing == null || !scopedCollegeCode.equals(existing.getCollegeCode())) {
                return ResultVO.error(403, "Forbidden");
            }
            teacherDTO.setCollegeCode(scopedCollegeCode);
        }
        teacherDTO.setTeacherNo(teacherNo);
        teacherService.updateTeacher(teacherDTO);
        return ResultVO.success();
    }

    @DeleteMapping("/{teacherNo}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Void> delete(@PathVariable String teacherNo, Authentication authentication) {
        String scopedCollegeCode = currentUserService.resolveManagedCollegeCode(authentication);
        if (scopedCollegeCode != null) {
            Teacher existing = teacherService.getById(teacherNo);
            if (existing == null || !scopedCollegeCode.equals(existing.getCollegeCode())) {
                return ResultVO.error(403, "Forbidden");
            }
        }
        teacherService.deleteTeacher(teacherNo);
        return ResultVO.success();
    }

    @GetMapping("/{teacherNo}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<Teacher> getById(@PathVariable String teacherNo, Authentication authentication) {
        String scopedCollegeCode = currentUserService.resolveManagedCollegeCode(authentication);
        if (scopedCollegeCode != null) {
            Teacher existing = teacherService.getById(teacherNo);
            if (existing == null || !scopedCollegeCode.equals(existing.getCollegeCode())) {
                return ResultVO.error(403, "Forbidden");
            }
        }
        Teacher teacher = teacherService.getById(teacherNo);
        if (dataScopeService.isStudent(authentication)) {
            DataScopeService.StudentArrangementScope scope = dataScopeService.resolveStudentArrangementScope(authentication);
            if (teacher == null || !scope.getTeacherNos().contains(teacher.getTeacherNo())) {
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
            @RequestParam(required = false) String collegeCode,
            @RequestParam(required = false) String department,
            Authentication authentication) {
        String scopedCollegeCode = currentUserService.resolveManagedCollegeCode(authentication);
        String effectiveCollegeCode = scopedCollegeCode != null ? scopedCollegeCode : collegeCode;
        if (scopedCollegeCode == null && currentUserService.isTeacher(authentication)) {
            effectiveCollegeCode = currentUserService.resolveCurrentCollegeCode(authentication);
        }
        if (scopedCollegeCode != null) {
            Page<Teacher> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<Teacher>()
                    .eq(Teacher::getCollegeCode, effectiveCollegeCode)
                    .like(teacherNo != null && !teacherNo.isBlank(), Teacher::getTeacherNo, teacherNo)
                    .like(name != null && !name.isBlank(), Teacher::getName, name);
            return ResultVO.success(teacherService.page(pageParam, wrapper));
        }
        if (dataScopeService.isStudent(authentication)) {
            DataScopeService.StudentArrangementScope scope = dataScopeService.resolveStudentArrangementScope(authentication);
            Page<Teacher> pageParam = new Page<>(page, size);
            if (scope.getTeacherNos().isEmpty()) {
                pageParam.setRecords(java.util.Collections.emptyList());
                pageParam.setTotal(0);
                return ResultVO.success(pageParam);
            }
            LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<Teacher>()
                    .in(Teacher::getTeacherNo, scope.getTeacherNos())
                    .like(teacherNo != null && !teacherNo.isBlank(), Teacher::getTeacherNo, teacherNo)
                    .like(name != null && !name.isBlank(), Teacher::getName, name);
            return ResultVO.success(teacherService.page(pageParam, wrapper));
        }
        if (effectiveCollegeCode != null) {
            Page<Teacher> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<Teacher>()
                    .eq(Teacher::getCollegeCode, effectiveCollegeCode)
                    .like(teacherNo != null && !teacherNo.isBlank(), Teacher::getTeacherNo, teacherNo)
                    .like(name != null && !name.isBlank(), Teacher::getName, name);
            return ResultVO.success(teacherService.page(pageParam, wrapper));
        }
        return ResultVO.success(teacherService.getTeacherPage(page, size, teacherNo, name, department));
    }

    @PutMapping("/{teacherNo}/status")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN')")
    public ResultVO<Void> updateStatus(@PathVariable String teacherNo, @RequestParam Integer status, Authentication authentication) {
        String scopedCollegeCode = currentUserService.resolveManagedCollegeCode(authentication);
        if (scopedCollegeCode != null) {
            Teacher existing = teacherService.getById(teacherNo);
            if (existing == null || !scopedCollegeCode.equals(existing.getCollegeCode())) {
                return ResultVO.error(403, "Forbidden");
            }
        }
        teacherService.updateTeacherStatus(teacherNo, status);
        return ResultVO.success();
    }
}