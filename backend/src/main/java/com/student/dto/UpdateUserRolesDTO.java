package com.student.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class UpdateUserRolesDTO {

    @NotEmpty(message = "Role set cannot be empty")
    private Set<String> roleCodes = new LinkedHashSet<>();
}
