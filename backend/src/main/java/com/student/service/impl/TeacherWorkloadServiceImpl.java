package com.student.service.impl;

import com.student.mapper.TeacherWorkloadMapper;
import com.student.service.TeacherWorkloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TeacherWorkloadServiceImpl implements TeacherWorkloadService {

    @Autowired
    private TeacherWorkloadMapper teacherWorkloadMapper;

    @Override
    public Map<String, Object> getTeacherWorkload(String teacherNo, String semester) {
        Map<String, Object> workload = teacherWorkloadMapper.selectTeachingWorkload(teacherNo, semester);
        
        // 计算工作量得分: 课时 * 1.0 + 监考时数 * 0.5 + 成绩记录数 * 0.1
        Integer totalHours = (Integer) workload.get("totalHours");
        Integer invigilateHours = (Integer) workload.get("invigilateHours");
        Integer totalScoreRecords = (Integer) workload.get("totalScoreRecords");
        
        BigDecimal workloadScore = BigDecimal.ZERO
            .add(new BigDecimal(totalHours != null ? totalHours : 0))
            .add(new BigDecimal(invigilateHours != null ? invigilateHours : 0).multiply(BigDecimal.valueOf(0.5)))
            .add(new BigDecimal(totalScoreRecords != null ? totalScoreRecords : 0).multiply(BigDecimal.valueOf(0.1)));
        
        workload.put("workloadScore", workloadScore.setScale(1, RoundingMode.HALF_UP));
        
        return workload;
    }

    @Override
    public List<Map<String, Object>> getTeachingStatistics(String collegeCode, String semester) {
        return teacherWorkloadMapper.selectTeachingStatistics(collegeCode, semester);
    }

    @Override
    public Map<String, Object> getWorkloadSummary(String teacherNo, String semester) {
        Map<String, Object> summary = new HashMap<>();
        
        summary.put("courseCount", teacherWorkloadMapper.countCoursesByTeacher(teacherNo, semester));
        summary.put("totalHours", teacherWorkloadMapper.sumHoursByTeacher(teacherNo, semester));
        summary.put("studentCount", teacherWorkloadMapper.countStudentsByTeacher(teacherNo, semester));
        summary.put("examCount", teacherWorkloadMapper.countExamsByTeacher(teacherNo, semester));
        summary.put("invigilateHours", teacherWorkloadMapper.countInvigilateHours(teacherNo, semester));
        summary.put("totalScoreRecords", teacherWorkloadMapper.countScoreRecords(teacherNo, semester));
        
        return summary;
    }
}
