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
        SCHOOL_ADMIN,
        COLLEGE_ADMIN,
        HOMEROOM_TEACHER,
        COURSE_TEACHER,
        STUDENT;

        public boolean isAdminGroup() {
            return this == SCHOOL_ADMIN || this == COLLEGE_ADMIN;
        }

        public boolean isTeacherGroup() {
            return this == HOMEROOM_TEACHER || this == COURSE_TEACHER;
        }

        public boolean isStudent() {
            return this == STUDENT;
        }
    }
}
