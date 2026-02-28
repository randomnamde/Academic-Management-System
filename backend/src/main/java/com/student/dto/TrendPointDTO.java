package com.student.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TrendPointDTO {

    private String periodLabel;

    private LocalDate startDate;

    private LocalDate endDate;

    private Long count;

    private Double avgScore;

    private Double passRate;

    private Double excellentRate;
}

