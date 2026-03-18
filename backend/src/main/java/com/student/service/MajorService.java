package com.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.dto.MajorDTO;
import com.student.entity.Major;

import java.util.List;

public interface MajorService extends IService<Major> {

    Page<Major> getMajorPage(Integer page, Integer size, String keyword, String collegeCode, Integer status, String scopedCollegeCode);

    List<Major> getMajorOptions(String collegeCode, Integer status, String scopedCollegeCode);

    Major getMajorDetail(String majorCode, String scopedCollegeCode);

    Major createMajor(MajorDTO dto, String scopedCollegeCode);

    void updateMajor(String majorCode, MajorDTO dto, String scopedCollegeCode);

    void updateMajorStatus(String majorCode, Integer status, String scopedCollegeCode);

    String generateMajorCode(String majorName, String majorAbbreviation);
}


