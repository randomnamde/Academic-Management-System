package com.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.entity.Class;

import java.time.Year;
import java.util.List;

public interface ClassService extends IService<Class> {

    Class getClassById(Long id);

    Class getClassByCode(String classCode);

    Class resolveClass(String classIdentifier);

    Page<Class> getClassPage(Integer page, Integer size, String className, String grade, Long teacherId, Long collegeId, String majorCode);

    List<Class> getClassesByTeacherId(Long teacherId);

    Class createClass(Class clazz);

    void updateClassByCode(String classCode, Class clazz);

    void deleteClassByCode(String classCode);

    String generateClassCode(Long collegeId, String majorCode, Year grade);
}
