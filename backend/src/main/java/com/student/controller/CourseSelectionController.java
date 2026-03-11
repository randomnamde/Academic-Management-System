package com.student.controller;

import com.student.dto.CourseSelectionRequestDTO;
import com.student.service.CourseSelectionService;
import com.student.common.Result;
import com.student.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/selection")
@RequiredArgsConstructor
public class CourseSelectionController {

    private final CourseSelectionService courseSelectionService;
    private final CurrentUserService currentUserService;

    @PostMapping("/select")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<String> selectCourse(@RequestBody CourseSelectionRequestDTO requestDTO, Authentication authentication) {
        requestDTO.setStudentId(currentUserService.getCurrentStudentNo(authentication));
        return courseSelectionService.selectCourse(requestDTO);
    }

    @GetMapping("/result")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<String> getResult(@RequestParam(required = false) String studentId,
                                    @RequestParam Long arrangementId,
                                    Authentication authentication) {
        studentId = currentUserService.getCurrentStudentNo(authentication);
        return courseSelectionService.getSelectionResult(studentId, arrangementId);
    }

    @PostMapping("/admin/preload")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public Result<String> preload() {
        return courseSelectionService.preloadInventory();
    }
}
