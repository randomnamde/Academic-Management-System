package com.student.controller;

import com.student.dto.CourseSelectionRequestDTO;
import com.student.service.CourseSelectionService;
import com.student.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/selection")
@RequiredArgsConstructor
public class CourseSelectionController {

    private final CourseSelectionService courseSelectionService;

    @PostMapping("/select")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<String> selectCourse(@RequestBody CourseSelectionRequestDTO requestDTO) {
        return courseSelectionService.selectCourse(requestDTO);
    }

    @GetMapping("/result")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<String> getResult(@RequestParam Long studentId, @RequestParam Long arrangementId) {
        return courseSelectionService.getSelectionResult(studentId, arrangementId);
    }

    @PostMapping("/admin/preload")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public Result<String> preload() {
        return courseSelectionService.preloadInventory();
    }
}
