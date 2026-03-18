package com.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.AlumniDTO;
import com.student.entity.Alumni;

public interface AlumniService {

    Page<Alumni> getAlumniPage(Integer page, Integer size, String studentId, String name,
                                 Integer graduationYear, String collegeCode, String majorCode);

    Alumni createAlumni(AlumniDTO dto);

    Alumni updateAlumni(Long id, AlumniDTO dto);

    void deleteAlumni(Long id);

    Alumni getByStudentId(String studentId);
}
