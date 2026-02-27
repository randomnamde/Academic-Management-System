package com.student.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.dto.LoginDTO;
import com.student.dto.RegisterDTO;
import com.student.dto.UpdateProfileDTO;
import com.student.entity.SysUser;
import com.student.exception.BusinessException;
import com.student.mapper.SysUserMapper;
import com.student.security.JwtTokenProvider;
import com.student.service.SysUserService;
import com.student.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        SysUser user = userMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException("Invalid username or password");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("Account is disabled");
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("Invalid username or password");
        }

        String token = jwtTokenProvider.generateToken(user);

        LoginVO loginVO = new LoginVO();
        BeanUtils.copyProperties(user, loginVO);
        loginVO.setToken(token);
        return loginVO;
    }

    @Override
    @Transactional
    public void register(RegisterDTO registerDTO) {
        if (userMapper.countByUsername(registerDTO.getUsername()) > 0) {
            throw new BusinessException("Username already exists");
        }

        SysUser user = new SysUser();
        BeanUtils.copyProperties(registerDTO, user);
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setStatus(1);

        userMapper.insert(user);
    }

    @Override
    public SysUser getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = userMapper.selectByIdWithPassword(userId);
        if (user == null) {
            throw new BusinessException("User not found");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        updateById(user);
    }

    @Override
    @Transactional
    public void updateStatus(Long userId, Integer status) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException("User not found");
        }

        user.setStatus(status);
        updateById(user);
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, UpdateProfileDTO dto) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException("User not found");
        }

        user.setRealName(dto.getRealName());
        user.setAvatar(dto.getAvatar());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        updateById(user);
    }
}
