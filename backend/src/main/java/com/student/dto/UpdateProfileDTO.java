package com.student.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpdateProfileDTO {

    @NotBlank(message = "Real name is required")
    private String realName;

    private String avatar;

    private String phone;

    private String email;
}
