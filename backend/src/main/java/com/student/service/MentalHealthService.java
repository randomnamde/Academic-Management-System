package com.student.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.entity.MentalHealthRecord;
import com.student.entity.MentalInterview;
import com.student.entity.MentalCrisis;

import java.util.List;

public interface MentalHealthService extends IService<MentalHealthRecord> {

    IPage<MentalHealthRecord> getRecordPage(String studentNo, String riskLevel, String assessmentType, Integer page, Integer size);

    List<MentalHealthRecord> getStudentRecords(String studentNo);

    boolean addRecord(MentalHealthRecord record);

    boolean updateRecord(MentalHealthRecord record);

    IPage<MentalInterview> getInterviewPage(String studentNo, String interviewType, Integer page, Integer size);

    boolean addInterview(MentalInterview interview);

    IPage<MentalCrisis> getCrisisPage(String studentNo, String riskLevel, Integer page, Integer size);

    boolean addCrisis(MentalCrisis crisis);

    boolean updateCrisis(MentalCrisis crisis);

    List<MentalHealthRecord> getRiskStudents(String riskLevel, Integer limit);
}
