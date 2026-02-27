package com.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.dto.TeacherDTO;
import com.student.entity.Teacher;

public interface TeacherService extends IService<Teacher> {

    void addTeacher(TeacherDTO teacherDTO);

    void updateTeacher(TeacherDTO teacherDTO);

    void deleteTeacher(Long id);

    Teacher getTeacherById(Long id);

    Page<Teacher> getTeacherPage(Integer page, Integer size, String teacherNo, String name, Long departmentId);

    void updateTeacherStatus(Long id, Integer status);
}
