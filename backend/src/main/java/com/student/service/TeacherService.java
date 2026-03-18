package com.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.dto.TeacherDTO;
import com.student.entity.Teacher;

public interface TeacherService extends IService<Teacher> {

    void addTeacher(TeacherDTO teacherDTO);

    void updateTeacher(TeacherDTO teacherDTO);

    void deleteTeacher(String teacherNo);

    Teacher getTeacherByNo(String teacherNo);

    Page<Teacher> getTeacherPage(Integer page, Integer size, String teacherNo, String name, String department);

    void updateTeacherStatus(String teacherNo, Integer status);

    String generateNextTeacherNo(String collegeCode);
}