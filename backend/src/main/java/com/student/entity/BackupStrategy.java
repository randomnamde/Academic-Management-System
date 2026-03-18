package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("backup_strategy")
public class BackupStrategy {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String strategyName;

    private String backupType;

    private String scheduleCron;

    private Integer retentionDays;

    private Integer isEnabled;

    private String tablesInclude;

    private String tablesExclude;

    private LocalDateTime lastExecuteTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
