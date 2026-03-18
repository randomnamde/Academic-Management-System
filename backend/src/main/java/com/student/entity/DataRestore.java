package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("data_restore")
public class DataRestore {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long backupId;

    private String restoreName;

    private String backupPath;

    private Integer tablesRestored;

    private String status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String errorMessage;

    private String operatorNo;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
