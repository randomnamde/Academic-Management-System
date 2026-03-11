package com.student.dto;

import com.student.entity.Attendance;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AttendanceDTO {

    private Long id;

    @NotNull(message = "Student id is required")
    private String studentId;

    @NotNull(message = "Course arrangement id is required")
    private Long courseArrangementId;

    @NotNull(message = "Attendance date is required")
    private LocalDate attendanceDate;

    @NotNull(message = "Attendance status is required")
    private Attendance.Status status;

    private LocalTime checkInTime;

    private LocalTime checkOutTime;

    private String remark;
}
