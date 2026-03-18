package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.StudentDTO;
import com.student.entity.Class;
import com.student.entity.Student;
import com.student.security.CurrentUserService;
import com.student.security.DataScopeService;
import com.student.service.ClassService;
import com.student.service.StudentService;
import com.student.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final CurrentUserService currentUserService;
    private final DataScopeService dataScopeService;
    private final ClassService classService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<Void> add(@RequestBody @Validated StudentDTO studentDTO, Authentication authentication) {
        studentDTO.setClassId(resolveClassCode(studentDTO.getClassId()));
        assertCollegeClassAccess(authentication, studentDTO.getClassId());
        studentService.addStudent(studentDTO);
        return ResultVO.success();
    }

    @PutMapping("/{studentNo}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<Void> update(@PathVariable String studentNo, @RequestBody @Validated StudentDTO studentDTO, Authentication authentication) {
        Student existing = studentService.getStudentByNo(studentNo);
        if (existing == null) {
            return ResultVO.error(404, "Student not found");
        }
        studentDTO.setClassId(resolveClassCode(studentDTO.getClassId()));
        assertCollegeStudentAccess(authentication, existing);
        assertCollegeClassAccess(authentication, studentDTO.getClassId());
        studentDTO.setStudentNo(existing.getStudentNo());
        studentService.updateStudent(studentDTO);
        return ResultVO.success();
    }

    @DeleteMapping("/{studentNo}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<Void> delete(@PathVariable String studentNo, Authentication authentication) {
        Student existing = studentService.getStudentByNo(studentNo);
        if (existing == null) {
            return ResultVO.error(404, "Student not found");
        }
        assertCollegeStudentAccess(authentication, existing);
        studentService.deleteStudent(studentNo);
        return ResultVO.success();
    }

    @GetMapping("/{studentNo}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<Student> getById(@PathVariable String studentNo, Authentication authentication) {
        if (currentUserService.isStudent(authentication)) {
            String currentStudentNo = currentUserService.getCurrentStudent(authentication).getStudentNo();
            if (!currentStudentNo.equals(studentNo)) {
                return ResultVO.error(403, "Forbidden");
            }
        }
        Student student = studentService.getStudentByNo(studentNo);
        if (student == null) {
            return ResultVO.error(404, "Student not found");
        }
        assertCollegeStudentAccess(authentication, student);
        return ResultVO.success(student);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<Page<Student>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String collegeCode,
            @RequestParam(required = false) String majorCode,
            @RequestParam(required = false) String classId,
            @RequestParam(required = false) Student.Status status,
            Authentication authentication) {
        if (currentUserService.isStudent(authentication)) {
            Student currentStudent = currentUserService.getCurrentStudent(authentication);
            Student detail = studentService.getStudentByNo(currentStudent.getStudentNo());
            Page<Student> singlePage = new Page<>(page, size);
            if (page == 1) {
                singlePage.setRecords(Collections.singletonList(detail));
                singlePage.setTotal(1);
            } else {
                singlePage.setRecords(Collections.emptyList());
                singlePage.setTotal(1);
            }
            return ResultVO.success(singlePage);
        }

        String scopedCollegeCode = currentUserService.resolveManagedCollegeCode(authentication);
        String effectiveCollegeCode = scopedCollegeCode != null ? scopedCollegeCode : collegeCode;
        if (scopedCollegeCode != null) {
            Set<String> classIds = dataScopeService.resolveCollegeClassCodes(authentication);
            if (classIds.isEmpty()) {
                Page<Student> emptyPage = new Page<>(page, size);
                emptyPage.setRecords(Collections.emptyList());
                emptyPage.setTotal(0);
                return ResultVO.success(emptyPage);
            }
            Page<Student> result = studentService.getStudentPage(
                    page,
                    size,
                    studentNo,
                    name,
                    classId,
                    effectiveCollegeCode,
                    majorCode,
                    status,
                    new ArrayList<>(classIds)
            );
            return ResultVO.success(result);
        }

        Page<Student> result = studentService.getStudentPage(page, size, studentNo, name, classId, effectiveCollegeCode, majorCode, status, null);
        return ResultVO.success(result);
    }

    @GetMapping("/statistics/gender")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<Map<String, Long>> getGenderStatistics() {
        return ResultVO.success(studentService.getGenderStatistics());
    }

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<List<Student>> getByClassId(@PathVariable String classId, Authentication authentication) {
        if (currentUserService.isStudent(authentication)) {
            Student currentStudent = currentUserService.getCurrentStudent(authentication);
            classId = currentStudent.getClassId();
        }
        assertCollegeClassAccess(authentication, classId);
        List<Student> students = studentService.getStudentsByClassId(classId);
        return ResultVO.success(students);
    }

    @PutMapping("/{studentNo}/status")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<Void> updateStatus(@PathVariable String studentNo, @RequestParam Student.Status status, Authentication authentication) {
        Student existing = studentService.getStudentByNo(studentNo);
        if (existing == null) {
            return ResultVO.error(404, "Student not found");
        }
        assertCollegeStudentAccess(authentication, existing);
        studentService.updateStudentStatus(studentNo, status);
        return ResultVO.success();
    }

    @GetMapping("/next-no")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<String> getNextStudentNo(
            @RequestParam String classId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate enrollmentDate,
            Authentication authentication) {
        assertCollegeClassAccess(authentication, classId);
        return ResultVO.success(studentService.generateStudentNo(classId, enrollmentDate));
    }

    private void assertCollegeClassAccess(Authentication authentication, String classId) {
        String scopedCollegeCode = currentUserService.resolveManagedCollegeCode(authentication);
        if (scopedCollegeCode == null || classId == null) {
            return;
        }
        Class clazz = classService.resolveClass(classId);
        if (clazz == null || !scopedCollegeCode.equals(clazz.getCollegeCode())) {
            throw new com.student.exception.BusinessException(403, "Forbidden");
        }
    }

    private String resolveClassCode(String classId) {
        Class clazz = classService.resolveClass(classId);
        return clazz == null ? classId : clazz.getClassCode();
    }

    private void assertCollegeStudentAccess(Authentication authentication, Student student) {
        if (student == null) {
            return;
        }
        assertCollegeClassAccess(authentication, student.getClassId());
    }
}




