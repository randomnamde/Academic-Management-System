package com.student.dto;

import lombok.Data;

@Data
public class RiskStudentDTO {

    private Long studentId;

    private String studentNo;

    private String studentName;

    private String className;

    private String riskType;

    private Long riskCount;

    private Double riskValue;
}

