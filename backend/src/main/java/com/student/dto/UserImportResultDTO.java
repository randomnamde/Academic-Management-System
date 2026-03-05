package com.student.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserImportResultDTO {
    private Integer totalCount = 0;
    private Integer successCount = 0;
    private Integer failedCount = 0;
    private List<UserImportSuccessItemDTO> successItems = new ArrayList<>();
    private List<UserImportFailItemDTO> failItems = new ArrayList<>();
}
