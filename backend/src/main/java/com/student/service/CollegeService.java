package com.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.dto.CollegeDTO;
import com.student.entity.College;

public interface CollegeService extends IService<College> {

    Page<College> getCollegePage(Integer page, Integer size, String keyword, Integer status, String scopedCollegeCode);

    College createCollege(CollegeDTO dto);

    void updateCollege(String collegeCode, CollegeDTO dto, String scopedCollegeCode);

    void updateCollegeStatus(String collegeCode, Integer status, String scopedCollegeCode);

    void bindAdmin(String collegeCode, String adminUsername, String scopedCollegeCode);
}