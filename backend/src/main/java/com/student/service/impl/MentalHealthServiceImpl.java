package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.entity.MentalCrisis;
import com.student.entity.MentalHealthRecord;
import com.student.entity.MentalInterview;
import com.student.mapper.MentalCrisisMapper;
import com.student.mapper.MentalHealthRecordMapper;
import com.student.mapper.MentalInterviewMapper;
import com.student.service.MentalHealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class MentalHealthServiceImpl extends ServiceImpl<MentalHealthRecordMapper, MentalHealthRecord> implements MentalHealthService {

    @Autowired
    private MentalInterviewMapper mentalInterviewMapper;

    @Autowired
    private MentalCrisisMapper mentalCrisisMapper;

    @Override
    public IPage<MentalHealthRecord> getRecordPage(String studentNo, String riskLevel, String assessmentType, Integer page, Integer size) {
        Page<MentalHealthRecord> pager = new Page<>(page, size);
        LambdaQueryWrapper<MentalHealthRecord> query = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(studentNo)) {
            query.eq(MentalHealthRecord::getStudentNo, studentNo);
        }
        if (StringUtils.hasText(riskLevel)) {
            query.eq(MentalHealthRecord::getRiskLevel, riskLevel);
        }
        if (StringUtils.hasText(assessmentType)) {
            query.eq(MentalHealthRecord::getAssessmentType, assessmentType);
        }
        query.orderByDesc(MentalHealthRecord::getRecordDate);
        return page(pager, query);
    }

    @Override
    public List<MentalHealthRecord> getStudentRecords(String studentNo) {
        return list(new LambdaQueryWrapper<MentalHealthRecord>()
                .eq(MentalHealthRecord::getStudentNo, studentNo)
                .orderByDesc(MentalHealthRecord::getRecordDate));
    }

    @Override
    @Transactional
    public boolean addRecord(MentalHealthRecord record) {
        // Auto calculate risk level based on score
        if (record.getTotalScore() != null) {
            if (record.getTotalScore().doubleValue() >= 160) {
                record.setRiskLevel("DANGER");
            } else if (record.getTotalScore().doubleValue() >= 120) {
                record.setRiskLevel("WARNING");
            } else {
                record.setRiskLevel("NORMAL");
            }
        }
        return save(record);
    }

    @Override
    public boolean updateRecord(MentalHealthRecord record) {
        return updateById(record);
    }

    @Override
    public IPage<MentalInterview> getInterviewPage(String studentNo, String interviewType, Integer page, Integer size) {
        Page<MentalInterview> pager = new Page<>(page, size);
        LambdaQueryWrapper<MentalInterview> query = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(studentNo)) {
            query.eq(MentalInterview::getStudentNo, studentNo);
        }
        if (StringUtils.hasText(interviewType)) {
            query.eq(MentalInterview::getInterviewType, interviewType);
        }
        query.orderByDesc(MentalInterview::getInterviewDate);
        return mentalInterviewMapper.selectPage(pager, query);
    }

    @Override
    @Transactional
    public boolean addInterview(MentalInterview interview) {
        return mentalInterviewMapper.insert(interview) > 0;
    }

    @Override
    public IPage<MentalCrisis> getCrisisPage(String studentNo, String riskLevel, Integer page, Integer size) {
        Page<MentalCrisis> pager = new Page<>(page, size);
        LambdaQueryWrapper<MentalCrisis> query = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(studentNo)) {
            query.eq(MentalCrisis::getStudentNo, studentNo);
        }
        if (StringUtils.hasText(riskLevel)) {
            query.eq(MentalCrisis::getRiskLevel, riskLevel);
        }
        query.orderByDesc(MentalCrisis::getCrisisDate);
        return mentalCrisisMapper.selectPage(pager, query);
    }

    @Override
    @Transactional
    public boolean addCrisis(MentalCrisis crisis) {
        return mentalCrisisMapper.insert(crisis) > 0;
    }

    @Override
    public boolean updateCrisis(MentalCrisis crisis) {
        return mentalCrisisMapper.updateById(crisis) > 0;
    }

    @Override
    public List<MentalHealthRecord> getRiskStudents(String riskLevel, Integer limit) {
        return list(new LambdaQueryWrapper<MentalHealthRecord>()
                .eq(MentalHealthRecord::getRiskLevel, riskLevel)
                .orderByDesc(MentalHealthRecord::getRecordDate)
                .last("LIMIT " + limit));
    }
}
