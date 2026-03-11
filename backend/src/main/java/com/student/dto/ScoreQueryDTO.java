package com.student.dto;

import lombok.Data;

@Data
public class ScoreQueryDTO {
    private String studentId;
    private Long teacherId;
    private Long courseArrangementId;
    private String semester;
    private Long collegeId;
    private String classId;
}
