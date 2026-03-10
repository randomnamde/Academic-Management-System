package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("selection_round")
public class SelectionRound {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String roundName;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Integer status; // 0-关闭, 1-开启
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
