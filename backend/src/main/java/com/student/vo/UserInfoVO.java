package com.student.vo;

import com.student.entity.SysUser;
import lombok.Data;

import java.util.List;
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

    private Integer status;

    private Long collegeId;

    private String collegeName;

    private SysUser.Role role;

    private String primaryRole;

    private Set<String> roles;

    private List<String> permissions;
}
