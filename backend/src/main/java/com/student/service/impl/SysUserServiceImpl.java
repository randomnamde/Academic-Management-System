package com.student.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.dto.LoginDTO;
import com.student.dto.RegisterDTO;
import com.student.dto.UpdateProfileDTO;
import com.student.entity.SysUser;
import com.student.entity.SysUserRole;
import com.student.exception.BusinessException;
import com.student.mapper.SysUserMapper;
import com.student.mapper.SysUserRoleMapper;
import com.student.security.JwtTokenProvider;
import com.student.security.PermissionService;
import com.student.security.RoleCode;
import com.student.service.SysUserService;
import com.student.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp");

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PermissionService permissionService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

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

        Set<RoleCode> roleCodes = getRoleCodes(user.getId());
        if (roleCodes.isEmpty()) {
            RoleCode fallback = RoleCode.fromLegacy(user.getRole());
            if (fallback != null) {
                roleCodes.add(fallback);
            }
        }
        if (roleCodes.isEmpty()) {
            throw new BusinessException("User has no available roles");
        }
        String primaryRole = roleCodes.iterator().next().name();
        String token = jwtTokenProvider.generateToken(user, roleCodes.stream().map(RoleCode::name).toList(), primaryRole);

        LoginVO loginVO = new LoginVO();
        BeanUtils.copyProperties(user, loginVO);
        loginVO.setToken(token);
        loginVO.setPrimaryRole(primaryRole);
        loginVO.setRoles(roleCodes.stream().map(RoleCode::name).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)));
        loginVO.setPermissions(permissionService.resolvePermissions(roleCodes));
        return loginVO;
    }

    @Override
    @Transactional
    public void register(RegisterDTO registerDTO) {
        throw new BusinessException(403, "当前系统已关闭自助注册，请联系管理员批量导入账号");
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

    @Override
    @Transactional
    public String uploadAvatar(Long userId, MultipartFile file) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException("User not found");
        }

        if (file == null || file.isEmpty()) {
            throw new BusinessException("Please select an avatar image");
        }

        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new BusinessException("Avatar size cannot exceed 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BusinessException("Only image files are supported");
        }

        String extension = resolveFileExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("Unsupported image format");
        }

        Path avatarDirectory = Paths.get(uploadDir, "avatars").toAbsolutePath().normalize();
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
        Path targetFile = avatarDirectory.resolve(fileName);

        try {
            Files.createDirectories(avatarDirectory);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new BusinessException("Avatar upload failed, please try again later");
        }

        String normalizedContextPath = (contextPath == null || contextPath.isBlank()) ? "" : contextPath;
        String avatarUrl = normalizedContextPath + "/public/avatars/" + fileName;
        user.setAvatar(avatarUrl);
        updateById(user);
        return avatarUrl;
    }

    @Override
    public Set<RoleCode> getRoleCodes(Long userId) {
        List<String> raw = sysUserRoleMapper.selectRoleCodesByUserId(userId);
        Set<RoleCode> result = new LinkedHashSet<>();
        for (String value : raw) {
            RoleCode code = RoleCode.from(value);
            if (code != null) {
                result.add(code);
            }
        }
        if (result.isEmpty()) {
            SysUser user = userMapper.selectById(userId);
            if (user != null) {
                RoleCode fallback = RoleCode.fromLegacy(user.getRole());
                if (fallback != null) {
                    result.add(fallback);
                }
            }
        }
        return result;
    }

    @Override
    @Transactional
    public void grantRole(Long userId, RoleCode roleCode) {
        if (userId == null || roleCode == null) {
            return;
        }
        Long count = sysUserRoleMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
                        .eq(SysUserRole::getRoleCode, roleCode.name()));
        if (count != null && count > 0) {
            return;
        }
        SysUserRole relation = new SysUserRole();
        relation.setUserId(userId);
        relation.setRoleCode(roleCode.name());
        sysUserRoleMapper.insert(relation);
    }

    private String resolveFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex >= filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex).toLowerCase(Locale.ROOT);
    }
}
