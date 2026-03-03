package com.student.vo;

import com.student.entity.SysUser;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class LoginVO {
    
    private Long id;
    
    private String username;
    
    private String realName;
    
    private String avatar;
    
    private String phone;
    
    private String email;
    
    private SysUser.Role role;

    private String primaryRole;

    private Set<String> roles;
    
    private String token;

    private List<String> permissions;
}
