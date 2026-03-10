package com.student.service;

import com.student.dto.CourseSelectionRequestDTO;
import com.student.common.Result;

public interface CourseSelectionService {
    Result<String> preloadInventory();
    Result<String> selectCourse(CourseSelectionRequestDTO requestDTO);
    Result<String> getSelectionResult(Long studentId, Long arrangementId);
}
