package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("student_reward_punishment")
public class StudentRewardPunishment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String studentId;

    @TableField(exist = false)
    private String studentName;

    @TableField(exist = false)
    private String className;

    private Type type;

    private String category;

    private String reason;

    private BigDecimal amount;

    private Status status;

    private String approverNo;

    @TableField(exist = false)
    private String approverName;

    private LocalDateTime approveTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public enum Type {
        REWARD, PUNISHMENT
    }

    public enum Status {
        PENDING, APPROVED, REJECTED
    }
}
