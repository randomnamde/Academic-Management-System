package com.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.entity.Class;

import java.util.List;

public interface ClassService extends IService<Class> {

    Class getClassById(Long id);

    Page<Class> getClassPage(Integer page, Integer size, String className, String grade, Long teacherId);

    List<Class> getClassesByTeacherId(Long teacherId);
}
