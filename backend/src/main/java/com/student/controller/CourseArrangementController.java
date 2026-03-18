package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.CourseArrangementDTO;
import com.student.entity.CourseArrangement;
import com.student.entity.Student;
import com.student.security.CurrentUserService;
import com.student.security.DataScopeService;
import com.student.service.ClassService;
import com.student.service.CourseArrangementService;
import com.student.service.StudentService;
import com.student.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/course-arrangement")
@RequiredArgsConstructor
public class CourseArrangementController {

    private final CourseArrangementService courseArrangementService;
    private final ClassService classService;
    private final CurrentUserService currentUserService;
    private final DataScopeService dataScopeService;
    private final StudentService studentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<Void> add(@RequestBody @Validated CourseArrangementDTO dto, Authentication authentication) {
        dto.setCollegeCode(resolveEffectiveCollegeCode(authentication, dto.getCollegeCode()));
        dto.setClassId(resolveClassCode(dto.getClassId()));
        assertCollegeAdminClassScope(authentication, dto.getClassId());
        if (currentUserService.isTeacher(authentication)) {
            String teacherNo = currentUserService.getCurrentTeacherNo(authentication);
            if (!teacherNo.equals(dto.getTeacherNo())) {
                return ResultVO.error(403, "Forbidden");
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
        dto.setCollegeCode(resolveEffectiveCollegeCode(authentication, dto.getCollegeCode()));
        dto.setClassId(resolveClassCode(dto.getClassId()));
        assertCollegeAdminClassScope(authentication, dto.getClassId());
        dto.setId(id);
        if (currentUserService.isTeacher(authentication)) {
            String teacherNo = currentUserService.getCurrentTeacherNo(authentication);
            if (!teacherNo.equals(dto.getTeacherNo())) {
                return ResultVO.error(403, "Forbidden");
            }
        }
        courseArrangementService.updateArrangement(dto);
        return ResultVO.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<Void> delete(@PathVariable Long id, Authentication authentication) {
        CourseArrangement detail = courseArrangementService.getArrangementById(id);
        if (detail == null) {
            return ResultVO.error(404, "Course arrangement not found");
        }
        assertCollegeAdminClassScope(authentication, detail.getClassId());
        if (currentUserService.isTeacher(authentication)) {
            String teacherNo = currentUserService.getCurrentTeacherNo(authentication);
            if (!teacherNo.equals(detail.getTeacherNo())) {
                return ResultVO.error(403, "Forbidden");
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
            String teacherNo = currentUserService.getCurrentTeacherNo(authentication);
            if (!teacherNo.equals(detail.getTeacherNo())) {
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
                                                  @RequestParam(required = false) String collegeCode,
                                                  @RequestParam(required = false) String courseCode,
                                                  @RequestParam(required = false) String teacherNo,
                                                  @RequestParam(required = false) String classId,
                                                  @RequestParam(required = false) String semester,
                                                  @RequestParam(required = false) Integer status,
                                                  Authentication authentication) {
        String scopedCollegeCode = dataScopeService.resolveScopedCollegeCode(authentication);
        String effectiveCollegeCode = scopedCollegeCode != null ? scopedCollegeCode : collegeCode;
        if (scopedCollegeCode == null && currentUserService.isTeacher(authentication)) {
            effectiveCollegeCode = currentUserService.resolveCurrentCollegeCode(authentication);
        }
        if (scopedCollegeCode != null && classId != null) {
            assertCollegeAdminClassScope(authentication, classId);
            classId = resolveClassCode(classId);
        }
        if (currentUserService.isTeacher(authentication)) {
            teacherNo = currentUserService.getCurrentTeacherNo(authentication);
        } else if (currentUserService.isStudent(authentication)) {
            Student student = currentUserService.getCurrentStudent(authentication);
            classId = student.getClassId();
            status = 1;
        }
        Page<CourseArrangement> result = courseArrangementService.getArrangementPage(page, size, effectiveCollegeCode, courseCode, teacherNo, classId, semester, status);
        if (scopedCollegeCode != null && classId == null) {
            Set<String> classCodes = dataScopeService.resolveCollegeClassCodes(authentication);
            result.setRecords(result.getRecords().stream().filter(item -> classCodes.contains(item.getClassId())).toList());
        }
        return ResultVO.success(result);
    }

    @GetMapping("/options")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<List<CourseArrangement>> options(@RequestParam(required = false) String teacherNo,
                                                     @RequestParam(required = false) String classId,
                                                     @RequestParam(required = false) Integer status,
                                                     Authentication authentication) {
        if (classId != null) {
            assertCollegeAdminClassScope(authentication, classId);
            classId = resolveClassCode(classId);
        }
        if (currentUserService.isTeacher(authentication)) {
            teacherNo = currentUserService.getCurrentTeacherNo(authentication);
        } else if (currentUserService.isStudent(authentication)) {
            Student student = currentUserService.getCurrentStudent(authentication);
            classId = student.getClassId();
            status = 1;
        }
        return ResultVO.success(courseArrangementService.getArrangementOptions(teacherNo, classId, status));
    }

    @GetMapping("/{id}/students")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<List<java.util.Map<String, String>>> arrangementStudents(@PathVariable Long id,
                                                                              Authentication authentication) {
        CourseArrangement arrangement = courseArrangementService.getArrangementById(id);
        if (arrangement == null) {
            return ResultVO.error(404, "Course arrangement not found");
        }
        assertCollegeAdminClassScope(authentication, arrangement.getClassId());
        List<Student> students = studentService.lambdaQuery().eq(Student::getClassId, arrangement.getClassId()).list();
        List<java.util.Map<String, String>> result = students.stream()
                .map(item -> {
                    java.util.Map<String, String> map = new LinkedHashMap<>();
                    map.put("studentNo", item.getStudentNo());
                    map.put("name", item.getName());
                    return map;
                })
                .collect(Collectors.toList());
        return ResultVO.success(result);
    }

    private void assertCollegeAdminClassScope(Authentication authentication, String classId) {
        String scopedCollegeCode = dataScopeService.resolveScopedCollegeCode(authentication);
        if (scopedCollegeCode == null || classId == null) {
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

    private String resolveEffectiveCollegeCode(Authentication authentication, String requestedCollegeCode) {
        String managedCollegeCode = currentUserService.resolveManagedCollegeCode(authentication);
        if (managedCollegeCode != null) {
            return managedCollegeCode;
        }
        if (currentUserService.isTeacher(authentication)) {
            String currentCollegeCode = currentUserService.resolveCurrentCollegeCode(authentication);
            if (currentCollegeCode != null) {
                return currentCollegeCode;
            }
        }
        return requestedCollegeCode;
    }
}
