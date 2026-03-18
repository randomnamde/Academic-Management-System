package com.student.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class StudentRewardPunishmentDTO {

    private Long id;

    @NotBlank(message = "学生ID不能为空")
    private String studentId;

    @NotNull(message = "类型不能为空")
    private String type;

    @NotBlank(message = "类别不能为空")
    private String category;

    @NotBlank(message = "原因不能为空")
    private String reason;

    private BigDecimal amount;

    private String status;
}
