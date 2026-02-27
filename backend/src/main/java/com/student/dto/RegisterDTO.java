package com.student.dto;

import com.student.entity.SysUser;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Data
public class RegisterDTO {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Real name is required")
    private String realName;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "Phone format is invalid")
    private String phone;

    private String email;

    @NotNull(message = "Role is required")
    private SysUser.Role role;
}
