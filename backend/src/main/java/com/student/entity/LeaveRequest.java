package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("leave_request")
public class LeaveRequest {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String studentId;
    
    @TableField(exist = false)
    private String studentNo;
    
    @TableField(exist = false)
    private String studentName;
    
    @TableField(exist = false)
    private String className;
    
    private Long courseArrangementId;
    
    @TableField(exist = false)
    private String courseName;
    
    private LeaveType leaveType;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private String reason;
    
    private String attachment;
    
    private Status status;

    private WorkflowType workflowType;

    private NodeCode currentNode;

    private FinalStatus finalStatus;
    
    private Long approverId;
    
    @TableField(exist = false)
    private String approverName;
    
    private LocalDateTime approveTime;
    
    private String approveRemark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    public enum LeaveType {
        SICK, PERSONAL, OFFICIAL, OTHER
    }
    
    public enum Status {
        PENDING, APPROVED, REJECTED
    }

    public enum WorkflowType {
        SHORT, LONG
    }

    public enum NodeCode {
        PENDING_HOMEROOM_REVIEW, PENDING_COLLEGE_REVIEW, COMPLETED
    }

    public enum FinalStatus {
        APPROVED, REJECTED
    }
}
