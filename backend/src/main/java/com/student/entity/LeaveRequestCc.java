package com.student.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("leave_request_cc")
public class LeaveRequestCc {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long leaveRequestId;

    private String receiverUserId;

    private String receiverTeacherNo;

    private Integer readFlag;

    private LocalDateTime readTime;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}