package com.student.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordVerificationDTO {

    @NotBlank(message = "Operator password is required")
    private String operatorPassword;
}
