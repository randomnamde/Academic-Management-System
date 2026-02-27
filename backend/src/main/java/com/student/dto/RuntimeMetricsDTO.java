package com.student.dto;

import lombok.Data;

@Data
public class RuntimeMetricsDTO {

    private String nodeStatus;

    private Integer gatewayConcurrency;

    private Long syncDelayMs;
}
