package com.student.vo;

import com.student.entity.SysUser;
import lombok.Data;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class UserInfoVO {
    private Long id;
    private String account;
    private String username;
    private String realName;
    private String avatar;
    private String phone;
    private String email;
    private SysUser.Role role;
    private String primaryRole;
    private Set<String> roles = new LinkedHashSet<>();
    private Set<String> permissions = new LinkedHashSet<>();
    private Integer status;

    private String classId;
    private String className;
    private String studentNo;

    private String collegeCode;
    private String collegeName;

    private String teacherNo;
    private String teacherDepartment;
}