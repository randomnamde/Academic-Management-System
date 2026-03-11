package com.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.dto.MajorDTO;
import com.student.entity.Major;

import java.util.List;

public interface MajorService extends IService<Major> {

    Page<Major> getMajorPage(Integer page, Integer size, String keyword, Long collegeId, Integer status, Long scopedCollegeId);

    List<Major> getMajorOptions(Long collegeId, Integer status, Long scopedCollegeId);

    Major getMajorDetail(String majorCode, Long scopedCollegeId);

    Major createMajor(MajorDTO dto, Long scopedCollegeId);

    void updateMajor(String majorCode, MajorDTO dto, Long scopedCollegeId);

    void updateMajorStatus(String majorCode, Integer status, Long scopedCollegeId);

    String generateMajorCode(String majorName, String majorAbbreviation);
}
