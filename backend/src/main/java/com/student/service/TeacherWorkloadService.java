package com.student.service;

import java.util.List;
import java.util.Map;

public interface TeacherWorkloadService {

    Map<String, Object> getTeacherWorkload(String teacherNo, String semester);

    List<Map<String, Object>> getTeachingStatistics(String collegeCode, String semester);

    Map<String, Object> getWorkloadSummary(String teacherNo, String semester);
}
