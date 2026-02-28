package com.student.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RiskStudentDTO {

    private Long studentId;

    private String studentNo;

    private String studentName;

    private String className;

    private String riskType;

    private Long riskCount;

    private Double riskValue;

    private String courseName;

    private Double score;

    private LocalDate attendanceDate;

    private String attendanceStatus;

    private Long leaveRequestId;

    private LocalDateTime submitTime;

    private Boolean overdue;

    private Double overdueHours;
}
