package com.student.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("semester")
public class Semester {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String semesterCode;

    private LocalDate startDate;

    private LocalDate endDate;

    private Status status;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public enum Status {
        PLANNED,
        ACTIVE,
        ENDED,
        ARCHIVED
    }
}
