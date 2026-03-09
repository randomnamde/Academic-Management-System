package com.student.service;

import java.util.List;

public interface SysConfigService {

    String getCurrentSemester();

    void setCurrentSemester(String semester);

    List<String> getCourseTimeSlots();

    void setCourseTimeSlots(List<String> timeSlots);
}
