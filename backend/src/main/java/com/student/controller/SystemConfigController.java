package com.student.controller;

import com.student.dto.CourseTimeSlotsDTO;
import com.student.service.SysConfigService;
import com.student.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SysConfigService sysConfigService;

    @GetMapping("/current-semester")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<Map<String, String>> getCurrentSemester() {
        return ResultVO.success(Map.of("currentSemester", sysConfigService.getCurrentSemester()));
    }

    @PutMapping("/current-semester")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResultVO<Void> setCurrentSemester(@RequestParam String currentSemester) {
        sysConfigService.setCurrentSemester(currentSemester);
        return ResultVO.success();
    }

    @GetMapping("/course-time-slots")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<Map<String, Object>> getCourseTimeSlots() {
        return ResultVO.success(Map.of("timeSlots", sysConfigService.getCourseTimeSlots()));
    }

    @PutMapping("/course-time-slots")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResultVO<Void> setCourseTimeSlots(@RequestBody @Validated CourseTimeSlotsDTO dto) {
        sysConfigService.setCourseTimeSlots(dto.getTimeSlots());
        return ResultVO.success();
    }
}


