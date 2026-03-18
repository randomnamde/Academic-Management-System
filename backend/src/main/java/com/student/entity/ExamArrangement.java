package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("exam_arrangement")
public class ExamArrangement {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String examCode;

    private Long courseArrangementId;

    @TableField(exist = false)
    private String courseName;

    @TableField(exist = false)
    private String teacherName;

    @TableField(exist = false)
    private String semester;

    private ExamType examType;

    private LocalDate examDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private String room;

    private Integer capacity;

    private Integer enrolledCount;

    private Status status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public enum ExamType {
        MIDTERM, FINAL, MAKEUP, RETAKE
    }

    public enum Status {
        SCHEDULED, ONGOING, COMPLETED, CANCELLED
    }
}
