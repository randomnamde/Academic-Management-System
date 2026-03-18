package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("data_backup")
public class DataBackup {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String backupName;

    private String backupType;

    private String filePath;

    private Long fileSize;

    private Integer tableCount;

    private String status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String errorMessage;

    private String operatorNo;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
