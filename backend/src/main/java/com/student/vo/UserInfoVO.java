package com.student.vo;

import com.student.entity.SysUser;
import lombok.Data;

import java.time.LocalDateTime;
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

    private LocalDateTime createTime;

    private Long collegeId;

    private String collegeName;

    private Long teacherId;

    private String teacherNo;

    private String teacherDepartment;

    private Long classId;

    private String className;

    private String studentNo;

    private SysUser.Role role;

    private String primaryRole;

    private Set<String> roles;

    private List<String> permissions;
}
