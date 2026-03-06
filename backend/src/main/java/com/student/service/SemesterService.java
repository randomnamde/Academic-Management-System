package com.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.dto.SemesterDTO;
import com.student.entity.Semester;

import java.util.List;

public interface SemesterService extends IService<Semester> {

    Page<Semester> getSemesterPage(Integer page, Integer size, String semesterCode, Semester.Status status);

    Semester addSemester(SemesterDTO dto);

    Semester updateSemester(Long id, SemesterDTO dto);

    Semester updateSemesterStatus(Long id, Semester.Status status);

    List<Semester> getSemesterOptions();

    Semester getActiveSemester();

    Semester activateSemesterByCode(String semesterCode);
}
