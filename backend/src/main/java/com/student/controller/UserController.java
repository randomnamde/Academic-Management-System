package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.UpdatePasswordDTO;
import com.student.dto.UpdateProfileDTO;
import com.student.entity.SysUser;
import com.student.service.SysUserService;
import com.student.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final SysUserService sysUserService;

    @GetMapping("/info")
    public ResultVO<SysUser> info(Authentication authentication) {
        if (authentication == null) {
            return ResultVO.error(401, "Unauthorized");
        }
        SysUser user = sysUserService.getByUsername(authentication.getName());
        if (user != null) {
            user.setPassword(null);
        }
        return ResultVO.success(user);
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultVO<Page<SysUser>> list(@RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer size,
                                        @RequestParam(required = false) String username,
                                        @RequestParam(required = false) String realName,
                                        @RequestParam(required = false) SysUser.Role role,
                                        @RequestParam(required = false) Integer status) {
        Page<SysUser> pageParam = new Page<>(page, size);
        Page<SysUser> result = sysUserService.lambdaQuery()
                .like(username != null && !username.isBlank(), SysUser::getUsername, username)
                .like(realName != null && !realName.isBlank(), SysUser::getRealName, realName)
                .eq(role != null, SysUser::getRole, role)
                .eq(status != null, SysUser::getStatus, status)
                .orderByDesc(SysUser::getCreateTime)
                .page(pageParam);
        return ResultVO.success(result);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultVO<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        sysUserService.updateStatus(id, status);
        return ResultVO.success();
    }

    @PutMapping("/password")
    public ResultVO<Void> updatePassword(@RequestBody @Validated UpdatePasswordDTO dto, Authentication authentication) {
        if (authentication == null) {
            return ResultVO.error(401, "Unauthorized");
        }
        SysUser currentUser = sysUserService.getByUsername(authentication.getName());
        if (currentUser == null) {
            return ResultVO.error(404, "User not found");
        }
        sysUserService.updatePassword(currentUser.getId(), dto.getOldPassword(), dto.getNewPassword());
        return ResultVO.success();
    }

    @PutMapping("/profile")
    public ResultVO<Void> updateProfile(@RequestBody @Validated UpdateProfileDTO dto, Authentication authentication) {
        if (authentication == null) {
            return ResultVO.error(401, "Unauthorized");
        }
        SysUser currentUser = sysUserService.getByUsername(authentication.getName());
        if (currentUser == null) {
            return ResultVO.error(404, "User not found");
        }
        sysUserService.updateProfile(currentUser.getId(), dto);
        return ResultVO.success();
    }

    @PostMapping("/avatar")
    public ResultVO<String> uploadAvatar(@RequestParam("file") MultipartFile file, Authentication authentication) {
        if (authentication == null) {
            return ResultVO.error(401, "Unauthorized");
        }
        SysUser currentUser = sysUserService.getByUsername(authentication.getName());
        if (currentUser == null) {
            return ResultVO.error(404, "User not found");
        }
        String avatarUrl = sysUserService.uploadAvatar(currentUser.getId(), file);
        return ResultVO.success(avatarUrl);
    }
}
