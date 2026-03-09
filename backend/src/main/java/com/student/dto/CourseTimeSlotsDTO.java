package com.student.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CourseTimeSlotsDTO {

    @NotEmpty(message = "请至少保留一个上课时间段")
    private List<@NotBlank(message = "上课时间段不能为空") String> timeSlots;
}
