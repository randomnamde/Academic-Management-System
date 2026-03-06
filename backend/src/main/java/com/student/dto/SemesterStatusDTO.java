package com.student.dto;

import com.student.entity.Semester;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SemesterStatusDTO {

    @NotNull(message = "请选择学期状态")
    private Semester.Status status;
}
