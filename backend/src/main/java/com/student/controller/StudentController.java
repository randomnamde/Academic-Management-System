package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.StudentDTO;
import com.student.entity.Student;
import com.student.service.StudentService;
import com.student.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<Void> add(@RequestBody @Validated StudentDTO studentDTO) {
        studentService.addStudent(studentDTO);
        return ResultVO.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<Void> update(@PathVariable Long id, @RequestBody @Validated StudentDTO studentDTO) {
        studentDTO.setId(id);
        studentService.updateStudent(studentDTO);
        return ResultVO.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<Void> delete(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResultVO.success();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<Student> getById(@PathVariable Long id) {
        Student student = studentService.getStudentById(id);
        return ResultVO.success(student);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<Page<Student>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Student.Status status) {
        Page<Student> result = studentService.getStudentPage(page, size, studentNo, name, classId, status);
        return ResultVO.success(result);
    }

    @GetMapping("/statistics/gender")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<Map<String, Long>> getGenderStatistics() {
        return ResultVO.success(studentService.getGenderStatistics());
    }

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<List<Student>> getByClassId(@PathVariable Long classId) {
        List<Student> students = studentService.getStudentsByClassId(classId);
        return ResultVO.success(students);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<Void> updateStatus(@PathVariable Long id, @RequestParam Student.Status status) {
        studentService.updateStudentStatus(id, status);
        return ResultVO.success();
    }
}
