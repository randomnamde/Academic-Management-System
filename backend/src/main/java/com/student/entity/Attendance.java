package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@TableName("attendance")
public class Attendance {
    
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
    
    private LocalDate attendanceDate;
    
    private Status status;
    
    private LocalTime checkInTime;
    
    private LocalTime checkOutTime;
    
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    public enum Status {
        PRESENT, ABSENT, LATE, LEAVE
    }
}
