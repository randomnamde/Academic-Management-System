package com.student.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("college")
public class College {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String collegeCode;

    private String collegeName;

    private String description;

    private Integer status;

    private String adminUserId;

    @TableField(exist = false)
    private String adminUsername;

    @TableField(exist = false)
    private Long majorCount;

    @TableField(exist = false)
    private Long classCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
