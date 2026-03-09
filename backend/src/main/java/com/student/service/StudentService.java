package com.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.dto.StudentDTO;
import com.student.entity.Student;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface StudentService extends IService<Student> {
    
    void addStudent(StudentDTO studentDTO);

    String generateStudentNo(Long classId, LocalDate enrollmentDate);
    
    void updateStudent(StudentDTO studentDTO);
    
    void deleteStudent(Long id);
    
    Student getStudentById(Long id);
    
    Page<Student> getStudentPage(Integer page,
                                 Integer size,
                                 String studentNo,
                                 String name,
                                 Long classId,
                                 Long collegeId,
                                 Student.Status status,
                                 List<Long> classIds);
    
    List<Student> getStudentsByClassId(Long classId);
    
    void updateStudentStatus(Long id, Student.Status status);
    
    Student getStudentByUserId(Long userId);

    Map<String, Long> getGenderStatistics();
}
