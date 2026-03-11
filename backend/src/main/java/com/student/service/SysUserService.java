package com.student.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.student.dto.LoginDTO;
import com.student.dto.RegisterDTO;
import com.student.dto.UpdateProfileDTO;
import com.student.entity.SysUser;
import com.student.security.RoleCode;
import com.student.vo.LoginVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public interface SysUserService extends IService<SysUser> {
    
    LoginVO login(LoginDTO loginDTO);
    
    void register(RegisterDTO registerDTO);
    
    SysUser getByUsername(String username);

    void verifyPassword(Long userId, String password);
    
    void updatePassword(Long userId, String oldPassword, String newPassword);

    void resetPasswordToInitialPassword(Long userId);

    int resetPasswordsToInitialPassword(java.util.List<Long> userIds);

    int resetAllPasswordsToInitialPassword();
    
    void updateStatus(Long userId, Integer status);

    void updateProfile(Long userId, UpdateProfileDTO dto);

    String uploadAvatar(Long userId, MultipartFile file);

    Set<RoleCode> getRoleCodes(Long userId);

    Map<Long, LinkedHashSet<String>> getRoleCodeNamesByUserIds(Set<Long> userIds);

    void grantRole(Long userId, RoleCode roleCode);

    void grantRole(String username, RoleCode roleCode);

    void assignRoles(Long userId, Set<RoleCode> roleCodes);
}
