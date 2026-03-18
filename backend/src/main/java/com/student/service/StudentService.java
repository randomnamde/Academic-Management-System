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

    String generateStudentNo(String classId, LocalDate enrollmentDate);
    
    void updateStudent(StudentDTO studentDTO);
    
    void deleteStudent(String studentNo);
    
    Student getStudentByNo(String studentNo);

    Page<Student> getStudentPage(Integer page,
                                 Integer size,
                                 String studentNo,
                                 String name,
                                 String classId,
                                 String collegeCode,
                                 String majorCode,
                                 Student.Status status,
                                 List<String> classIds);
    
    List<Student> getStudentsByClassId(String classId);
    
    void updateStudentStatus(String studentNo, Student.Status status);
    
    Student getStudentByUserId(String userId);

    Map<String, Long> getGenderStatistics();
}


