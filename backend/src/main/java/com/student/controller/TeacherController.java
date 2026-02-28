package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.dto.TeacherDTO;
import com.student.entity.Teacher;
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

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResultVO<Void> add(@RequestBody @Validated TeacherDTO teacherDTO) {
        teacherService.addTeacher(teacherDTO);
        return ResultVO.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultVO<Void> update(@PathVariable Long id, @RequestBody @Validated TeacherDTO teacherDTO) {
        teacherDTO.setId(id);
        teacherService.updateTeacher(teacherDTO);
        return ResultVO.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultVO<Void> delete(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return ResultVO.success();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<Teacher> getById(@PathVariable Long id, Authentication authentication) {
        if (dataScopeService.isStudent(authentication)) {
            DataScopeService.StudentArrangementScope scope = dataScopeService.resolveStudentArrangementScope(authentication);
            if (!scope.getTeacherIds().contains(id)) {
                return ResultVO.error(403, "Forbidden");
            }
        }
        Teacher teacher = teacherService.getTeacherById(id);
        return ResultVO.success(teacher);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<Page<Teacher>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String teacherNo,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long departmentId,
            Authentication authentication) {
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
        Page<Teacher> result = teacherService.getTeacherPage(page, size, teacherNo, name, departmentId);
        return ResultVO.success(result);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultVO<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        teacherService.updateTeacherStatus(id, status);
        return ResultVO.success();
    }
}
