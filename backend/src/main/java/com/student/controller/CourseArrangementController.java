package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.CourseArrangementDTO;
import com.student.entity.CourseArrangement;
import com.student.entity.Student;
import com.student.entity.SysUser;
import com.student.security.CurrentUserService;
import com.student.service.CourseArrangementService;
import com.student.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/course-arrangement")
@RequiredArgsConstructor
public class CourseArrangementController {

    private final CourseArrangementService courseArrangementService;
    private final CurrentUserService currentUserService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<Void> add(@RequestBody @Validated CourseArrangementDTO dto, Authentication authentication) {
        SysUser user = currentUserService.getCurrentUser(authentication);
        if (user.getRole() == SysUser.Role.TEACHER) {
            Long teacherId = currentUserService.getCurrentTeacherId(authentication);
            if (!teacherId.equals(dto.getTeacherId())) {
                return ResultVO.error(403, "Teacher can only create own arrangements");
            }
        }
        courseArrangementService.addArrangement(dto);
        return ResultVO.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<Void> update(@PathVariable Long id,
                                 @RequestBody @Validated CourseArrangementDTO dto,
                                 Authentication authentication) {
        dto.setId(id);
        SysUser user = currentUserService.getCurrentUser(authentication);
        if (user.getRole() == SysUser.Role.TEACHER) {
            Long teacherId = currentUserService.getCurrentTeacherId(authentication);
            if (!teacherId.equals(dto.getTeacherId())) {
                return ResultVO.error(403, "Teacher can only update own arrangements");
            }
        }
        courseArrangementService.updateArrangement(dto);
        return ResultVO.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<Void> delete(@PathVariable Long id, Authentication authentication) {
        SysUser user = currentUserService.getCurrentUser(authentication);
        if (user.getRole() == SysUser.Role.TEACHER) {
            CourseArrangement detail = courseArrangementService.getArrangementById(id);
            if (detail == null) {
                return ResultVO.error(404, "Course arrangement not found");
            }
            Long teacherId = currentUserService.getCurrentTeacherId(authentication);
            if (!teacherId.equals(detail.getTeacherId())) {
                return ResultVO.error(403, "Teacher can only delete own arrangements");
            }
        }
        courseArrangementService.deleteArrangement(id);
        return ResultVO.success();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<CourseArrangement> getById(@PathVariable Long id, Authentication authentication) {
        CourseArrangement detail = courseArrangementService.getArrangementById(id);
        if (detail == null) {
            return ResultVO.error(404, "Course arrangement not found");
        }

        SysUser user = currentUserService.getCurrentUser(authentication);
        if (user.getRole() == SysUser.Role.TEACHER) {
            Long teacherId = currentUserService.getCurrentTeacherId(authentication);
            if (!teacherId.equals(detail.getTeacherId())) {
                return ResultVO.error(403, "Forbidden");
            }
        }
        if (user.getRole() == SysUser.Role.STUDENT) {
            Student student = currentUserService.getCurrentStudent(authentication);
            if (student.getClassId() == null || !student.getClassId().equals(detail.getClassId())) {
                return ResultVO.error(403, "Forbidden");
            }
        }
        return ResultVO.success(detail);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<Page<CourseArrangement>> list(@RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer size,
                                                  @RequestParam(required = false) Long courseId,
                                                  @RequestParam(required = false) Long teacherId,
                                                  @RequestParam(required = false) Long classId,
                                                  @RequestParam(required = false) String semester,
                                                  @RequestParam(required = false) Integer status,
                                                  Authentication authentication) {
        SysUser user = currentUserService.getCurrentUser(authentication);
        if (user.getRole() == SysUser.Role.TEACHER) {
            teacherId = currentUserService.getCurrentTeacherId(authentication);
        } else if (user.getRole() == SysUser.Role.STUDENT) {
            Student student = currentUserService.getCurrentStudent(authentication);
            classId = student.getClassId();
            status = 1;
        }
        return ResultVO.success(courseArrangementService.getArrangementPage(page, size, courseId, teacherId, classId, semester, status));
    }

    @GetMapping("/options")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<List<CourseArrangement>> options(@RequestParam(required = false) Long teacherId,
                                                     @RequestParam(required = false) Long classId,
                                                     @RequestParam(required = false) Integer status,
                                                     Authentication authentication) {
        SysUser user = currentUserService.getCurrentUser(authentication);
        if (user.getRole() == SysUser.Role.TEACHER) {
            teacherId = currentUserService.getCurrentTeacherId(authentication);
        } else if (user.getRole() == SysUser.Role.STUDENT) {
            Student student = currentUserService.getCurrentStudent(authentication);
            classId = student.getClassId();
            status = 1;
        }
        return ResultVO.success(courseArrangementService.getArrangementOptions(teacherId, classId, status));
    }
}
