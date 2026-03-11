package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.CourseArrangementDTO;
import com.student.entity.CourseArrangement;
import com.student.entity.Student;
import com.student.security.CurrentUserService;
import com.student.security.DataScopeService;
import com.student.service.ClassService;
import com.student.service.CourseArrangementService;
import com.student.service.SysConfigService;
import com.student.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/course-arrangement")
@RequiredArgsConstructor
public class CourseArrangementController {

    private final CourseArrangementService courseArrangementService;
    private final ClassService classService;
    private final CurrentUserService currentUserService;
    private final DataScopeService dataScopeService;
    private final SysConfigService sysConfigService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<Void> add(@RequestBody @Validated CourseArrangementDTO dto, Authentication authentication) {
        dto.setCollegeId(resolveEffectiveCollegeId(authentication, dto.getCollegeId()));
        dto.setClassId(resolveClassCode(dto.getClassId()));
        assertCollegeAdminClassScope(authentication, dto.getClassId());
        if (currentUserService.isTeacher(authentication)) {
            Long teacherId = currentUserService.getCurrentTeacherId(authentication);
            if (!teacherId.equals(dto.getTeacherId())) {
                return ResultVO.error(403, "教师只能为本人创建排课");
            }
        }
        courseArrangementService.addArrangement(dto);
        return ResultVO.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<Void> update(@PathVariable Long id,
                                 @RequestBody @Validated CourseArrangementDTO dto,
                                 Authentication authentication) {
        dto.setCollegeId(resolveEffectiveCollegeId(authentication, dto.getCollegeId()));
        dto.setClassId(resolveClassCode(dto.getClassId()));
        assertCollegeAdminClassScope(authentication, dto.getClassId());
        dto.setId(id);
        if (currentUserService.isTeacher(authentication)) {
            Long teacherId = currentUserService.getCurrentTeacherId(authentication);
            if (!teacherId.equals(dto.getTeacherId())) {
                return ResultVO.error(403, "教师只能修改本人排课");
            }
        }
        courseArrangementService.updateArrangement(dto);
        return ResultVO.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<Void> delete(@PathVariable Long id, Authentication authentication) {
        CourseArrangement target = courseArrangementService.getArrangementById(id);
        if (target != null) {
            assertCollegeAdminClassScope(authentication, target.getClassId());
        }
        if (currentUserService.isTeacher(authentication)) {
            CourseArrangement detail = courseArrangementService.getArrangementById(id);
            if (detail == null) {
                return ResultVO.error(404, "Course arrangement not found");
            }
            Long teacherId = currentUserService.getCurrentTeacherId(authentication);
            if (!teacherId.equals(detail.getTeacherId())) {
                return ResultVO.error(403, "教师只能删除本人排课");
            }
        }
        courseArrangementService.deleteArrangement(id);
        return ResultVO.success();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<CourseArrangement> getById(@PathVariable Long id, Authentication authentication) {
        CourseArrangement detail = courseArrangementService.getArrangementById(id);
        if (detail == null) {
            return ResultVO.error(404, "Course arrangement not found");
        }
        assertCollegeAdminClassScope(authentication, detail.getClassId());

        if (currentUserService.isTeacher(authentication)) {
            Long teacherId = currentUserService.getCurrentTeacherId(authentication);
            if (!teacherId.equals(detail.getTeacherId())) {
                return ResultVO.error(403, "Forbidden");
            }
        }
        if (currentUserService.isStudent(authentication)) {
            Student student = currentUserService.getCurrentStudent(authentication);
            if (student.getClassId() == null || !student.getClassId().equals(detail.getClassId())) {
                return ResultVO.error(403, "Forbidden");
            }
        }
        return ResultVO.success(detail);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<Page<CourseArrangement>> list(@RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer size,
                                                  @RequestParam(required = false) Long collegeId,
                                                  @RequestParam(required = false) Long courseId,
                                                  @RequestParam(required = false) Long teacherId,
                                                  @RequestParam(required = false) String classId,
                                                  @RequestParam(required = false) String semester,
                                                  @RequestParam(required = false) Integer status,
                                                  Authentication authentication) {
        Long scopedCollegeId = dataScopeService.resolveScopedCollegeId(authentication);
        Long effectiveCollegeId = scopedCollegeId != null ? scopedCollegeId : collegeId;
        if (scopedCollegeId == null && currentUserService.isTeacher(authentication)) {
            effectiveCollegeId = currentUserService.resolveCurrentCollegeId(authentication);
        }
        if (scopedCollegeId != null && classId != null) {
            assertCollegeAdminClassScope(authentication, classId);
            classId = resolveClassCode(classId);
        }
        if (currentUserService.isTeacher(authentication)) {
            teacherId = currentUserService.getCurrentTeacherId(authentication);
        } else if (currentUserService.isStudent(authentication)) {
            Student student = currentUserService.getCurrentStudent(authentication);
            if (student.getClassId() == null) {
                return ResultVO.success(new Page<>(page, size));
            }
            classId = student.getClassId();
            if (semester == null || semester.isBlank()) {
                semester = sysConfigService.getCurrentSemester();
            }
            status = 1;
        }
        Page<CourseArrangement> result = courseArrangementService.getArrangementPage(page, size, effectiveCollegeId, courseId, teacherId, classId, semester, status);
        if (scopedCollegeId != null && classId == null) {
            Set<String> classIds = dataScopeService.resolveCollegeClassCodes(authentication);
            result.setRecords(result.getRecords().stream().filter(item -> classIds.contains(item.getClassId())).toList());
        }
        return ResultVO.success(result);
    }

    @GetMapping("/options")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<List<CourseArrangement>> options(@RequestParam(required = false) Long teacherId,
                                                     @RequestParam(required = false) String classId,
                                                     @RequestParam(required = false) Integer status,
                                                     Authentication authentication) {
        if (classId != null) {
            assertCollegeAdminClassScope(authentication, classId);
            classId = resolveClassCode(classId);
        }
        if (currentUserService.isTeacher(authentication)) {
            teacherId = currentUserService.getCurrentTeacherId(authentication);
        } else if (currentUserService.isStudent(authentication)) {
            Student student = currentUserService.getCurrentStudent(authentication);
            classId = student.getClassId();
            status = 1;
        }
        List<CourseArrangement> result = courseArrangementService.getArrangementOptions(teacherId, classId, status);
        Long scopedCollegeId = dataScopeService.resolveScopedCollegeId(authentication);
        if (scopedCollegeId != null && classId == null) {
            Set<String> classIds = dataScopeService.resolveCollegeClassCodes(authentication);
            result = result.stream().filter(item -> classIds.contains(item.getClassId())).toList();
        }
        return ResultVO.success(result);
    }

    private void assertCollegeAdminClassScope(Authentication authentication, String classId) {
        Long scopedCollegeId = dataScopeService.resolveScopedCollegeId(authentication);
        if (scopedCollegeId == null || classId == null) {
            return;
        }
        String resolvedClassCode = resolveClassCode(classId);
        Set<String> classCodes = dataScopeService.resolveCollegeClassCodes(authentication);
        if (!classCodes.contains(resolvedClassCode)) {
            throw new com.student.exception.BusinessException(403, "Forbidden");
        }
    }

    private String resolveClassCode(String classId) {
        var clazz = classService.resolveClass(classId);
        return clazz == null ? classId : clazz.getClassCode();
    }

    private Long resolveEffectiveCollegeId(Authentication authentication, Long requestedCollegeId) {
        Long managedCollegeId = currentUserService.resolveManagedCollegeId(authentication);
        if (managedCollegeId != null) {
            return managedCollegeId;
        }
        if (currentUserService.isTeacher(authentication)) {
            Long currentCollegeId = currentUserService.resolveCurrentCollegeId(authentication);
            if (currentCollegeId != null) {
                return currentCollegeId;
            }
        }
        return requestedCollegeId;
    }
}
