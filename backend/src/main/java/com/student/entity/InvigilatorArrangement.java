package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("invigilator_arrangement")
public class InvigilatorArrangement {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long examArrangementId;

    @TableField(exist = false)
    private String examCode;

    @TableField(exist = false)
    private String courseName;

    @TableField(exist = false)
    private String examDate;

    @TableField(exist = false)
    private String room;

    private String teacherNo;

    @TableField(exist = false)
    private String teacherName;

    private Role role;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public enum Role {
        PRIMARY, SECONDARY
    }
}
