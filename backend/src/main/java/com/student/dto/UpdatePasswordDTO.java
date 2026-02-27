package com.student.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpdatePasswordDTO {

    @NotBlank(message = "Old password is required")
    private String oldPassword;

    @NotBlank(message = "New password is required")
    private String newPassword;
}
