package com.student.vo;

import com.student.entity.SysUser;
import lombok.Data;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class RbacUserListItemVO {

    private Long id;

    private String username;

    private String realName;

    private SysUser.Role role;

    private String primaryRole;

    private Set<String> roles = new LinkedHashSet<>();

    private String phone;

    private String email;

    private Integer status;

    private Long collegeId;

    private String collegeName;

    private Long classId;

    private String classDisplayName;
}
