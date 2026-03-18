package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("alumni")
public class Alumni {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("student_id")
    private String studentId;

    @TableField("name")
    private String name;

    @TableField("gender")
    private Integer gender;

    @TableField("class_code")
    private String classCode;

    @TableField("graduation_year")
    private Integer graduationYear;

    @TableField("major_code")
    private String majorCode;

    @TableField("college_code")
    private String collegeCode;

    @TableField("phone")
    private String phone;

    @TableField("email")
    private String email;

    @TableField("wechat")
    private String wechat;

    @TableField("current_company")
    private String currentCompany;

    @TableField("current_position")
    private String currentPosition;

    @TableField("work_years")
    private Integer workYears;

    @TableField("alumni_association_member")
    private Integer alumniAssociationMember;

    @TableField("volunteer")
    private Integer volunteer;

    @TableField("donation")
    private Integer donation;

    @TableField("status")
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 关联字段（不映射数据库）
    @TableField(exist = false)
    private String className;

    @TableField(exist = false)
    private String collegeName;

    @TableField(exist = false)
    private String majorName;
}
