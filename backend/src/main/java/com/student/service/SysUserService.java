package com.student.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.student.dto.LoginDTO;
import com.student.dto.RegisterDTO;
import com.student.dto.UpdateProfileDTO;
import com.student.entity.SysUser;
import com.student.vo.LoginVO;

public interface SysUserService extends IService<SysUser> {
    
    LoginVO login(LoginDTO loginDTO);
    
    void register(RegisterDTO registerDTO);
    
    SysUser getByUsername(String username);
    
    void updatePassword(Long userId, String oldPassword, String newPassword);
    
    void updateStatus(Long userId, Integer status);

    void updateProfile(Long userId, UpdateProfileDTO dto);
}
