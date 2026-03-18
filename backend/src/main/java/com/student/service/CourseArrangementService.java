package com.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.dto.CourseArrangementDTO;
import com.student.entity.CourseArrangement;

import java.util.List;

public interface CourseArrangementService extends IService<CourseArrangement> {

    void addArrangement(CourseArrangementDTO dto);

    void updateArrangement(CourseArrangementDTO dto);

    void deleteArrangement(Long id);

    CourseArrangement getArrangementById(Long id);

    Page<CourseArrangement> getArrangementPage(Integer page,
                                               Integer size,
                                               String collegeCode,
                                               String courseCode,
                                               String teacherNo,
                                               String classId,
                                               String semester,
                                               Integer status);

    List<CourseArrangement> getArrangementOptions(String teacherNo, String classId, Integer status);
}


