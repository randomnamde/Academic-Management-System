package com.student.dto;

import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class CourseArrangementDTO {

    private Long id;

    @NotNull(message = "请选择学院")
    private Long collegeId;

    @NotNull(message = "请选择课程")
    private Long courseId;

    @NotNull(message = "请选择教师")
    private Long teacherId;

    @NotNull(message = "请选择班级")
    private String classId;

    @NotBlank(message = "请输入学期")
    private String semester;

    @NotBlank(message = "请输入时间安排")
    private String schedule;

    private String room;

    @NotNull(message = "请输入容量")
    @Min(value = 1, message = "容量必须大于0")
    private Integer capacity;

    private Integer status;
}
