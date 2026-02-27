package com.student.dto;

import com.student.entity.LeaveRequest;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class LeaveRequestDTO {

    private Long id;

    @NotNull(message = "Student id is required")
    private Long studentId;

    @NotNull(message = "Course arrangement id is required")
    private Long courseArrangementId;

    @NotNull(message = "Leave type is required")
    private LeaveRequest.LeaveType leaveType;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    @NotBlank(message = "Reason is required")
    private String reason;

    private String attachment;

    private String approveRemark;
}
