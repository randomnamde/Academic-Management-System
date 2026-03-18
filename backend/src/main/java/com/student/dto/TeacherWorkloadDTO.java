package com.student.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TeacherWorkloadDTO {
    private String teacherNo;
    private String teacherName;
    private Integer courseCount;
    private Integer totalHours;
    private Integer studentCount;
    private Integer examCount;
    private Integer invigilateHours;
    private Integer totalScoreRecords;
    private BigDecimal workloadScore;
}
