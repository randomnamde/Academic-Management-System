package com.student.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AnalyticsFilterDTO {

    private LocalDate startDate;
    private LocalDate endDate;
    private String semester;
    private String classId;
    private String teacherNo;
}