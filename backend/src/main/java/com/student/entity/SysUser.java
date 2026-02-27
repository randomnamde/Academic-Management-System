package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    @TableField(select = false)
    private String password;
    
    private String realName;
    
    private String avatar;
    
    private String phone;
    
    private String email;
    
    private Integer status;
    
    private Role role;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    public enum Role {
        ADMIN, TEACHER, STUDENT
    }
}
