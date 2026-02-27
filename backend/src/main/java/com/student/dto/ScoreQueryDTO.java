package com.student.dto;

import lombok.Data;

@Data
public class ScoreQueryDTO {
    private Long studentId;
    private Long courseArrangementId;
    private String semester;
    private Long classId;
}
