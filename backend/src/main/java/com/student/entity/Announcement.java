package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("announcement")
public class Announcement {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    
    private String content;
    
    private Type type;
    
    private TargetRole targetRole;
    
    private String targetClassId;
    
    @TableField(exist = false)
    private String targetClassName;
    
    private Integer priority;
    
    private Long authorId;
    
    @TableField(exist = false)
    private String authorName;
    
    private Integer viewCount;
    
    private Integer isTop;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    public enum Type {
        NOTICE, NEWS, EVENT, IMPORTANT
    }
    
    public enum TargetRole {
        ALL, SCHOOL_ADMIN, COLLEGE_ADMIN, HOMEROOM_TEACHER, COURSE_TEACHER, STUDENT
    }
}
