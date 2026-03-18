package com.student.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("leave_request_approval")
public class LeaveRequestApproval {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long leaveRequestId;

    private String nodeCode;

    private String approverUserId;

    private String approverTeacherNo;

    private String decision;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime operateTime;
}