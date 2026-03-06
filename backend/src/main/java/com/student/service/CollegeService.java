package com.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.dto.CollegeDTO;
import com.student.entity.College;

public interface CollegeService extends IService<College> {

    Page<College> getCollegePage(Integer page, Integer size, String keyword, Integer status, Long scopedCollegeId);

    College createCollege(CollegeDTO dto);

    void updateCollege(Long id, CollegeDTO dto, Long scopedCollegeId);

    void updateCollegeStatus(Long id, Integer status, Long scopedCollegeId);

    void bindAdmin(Long id, String adminUsername, Long scopedCollegeId);
}
