package com.student.dto;

import lombok.Data;

@Data
public class ScoreQueryDTO {
    private String studentId;
    private String teacherNo;
    private Long courseArrangementId;
    private String semester;
    private String collegeCode;
    private String classCode;
}