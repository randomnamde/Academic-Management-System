package com.student.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ExamArrangementDTO {

    private Long id;

    @NotBlank(message = "考试编号不能为空")
    private String examCode;

    @NotNull(message = "授课安排ID不能为空")
    private Long courseArrangementId;

    @NotNull(message = "考试类型不能为空")
    private String examType;

    @NotNull(message = "考试日期不能为空")
    private LocalDate examDate;

    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;

    @NotBlank(message = "考场不能为空")
    private String room;

    private Integer capacity;

    private String status;
}
