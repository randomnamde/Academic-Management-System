package com.student.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.student.entity.MentalCrisis;
import com.student.entity.MentalHealthRecord;
import com.student.entity.MentalInterview;
import com.student.security.CurrentUserService;
import com.student.service.MentalHealthService;
import com.student.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mental-health")
@RequiredArgsConstructor
public class MentalHealthController {

    private final MentalHealthService mentalHealthService;
    private final CurrentUserService currentUserService;

    // ==================== Health Records ====================

    @GetMapping("/record/list")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COUNSELOR')")
    public ResultVO<IPage<MentalHealthRecord>> getRecordList(
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String assessmentType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return ResultVO.success(mentalHealthService.getRecordPage(studentNo, riskLevel, assessmentType, page, size));
    }

    @GetMapping("/record/student/{studentNo}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COUNSELOR', 'STUDENT')")
    public ResultVO<List<MentalHealthRecord>> getStudentRecords(@PathVariable String studentNo) {
        return ResultVO.success(mentalHealthService.getStudentRecords(studentNo));
    }

    @PostMapping("/record")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'COUNSELOR')")
    public ResultVO<Void> addRecord(@RequestBody MentalHealthRecord record, Authentication authentication) {
        record.setAssessorNo(currentUserService.getCurrentTeacherNo(authentication));
        mentalHealthService.addRecord(record);
        return ResultVO.success();
    }

    @PutMapping("/record/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'COUNSELOR')")
    public ResultVO<Void> updateRecord(@PathVariable Long id, @RequestBody MentalHealthRecord record) {
        record.setId(id);
        mentalHealthService.updateRecord(record);
        return ResultVO.success();
    }

    // ==================== Interviews ====================

    @GetMapping("/interview/list")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COUNSELOR')")
    public ResultVO<IPage<MentalInterview>> getInterviewList(
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String interviewType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return ResultVO.success(mentalHealthService.getInterviewPage(studentNo, interviewType, page, size));
    }

    @PostMapping("/interview")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'COUNSELOR')")
    public ResultVO<Void> addInterview(@RequestBody MentalInterview interview, Authentication authentication) {
        interview.setInterviewerNo(currentUserService.getCurrentTeacherNo(authentication));
        mentalHealthService.addInterview(interview);
        return ResultVO.success();
    }

    // ==================== Crises ====================

    @GetMapping("/crisis/list")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'COUNSELOR')")
    public ResultVO<IPage<MentalCrisis>> getCrisisList(
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return ResultVO.success(mentalHealthService.getCrisisPage(studentNo, riskLevel, page, size));
    }

    @PostMapping("/crisis")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'COUNSELOR')")
    public ResultVO<Void> addCrisis(@RequestBody MentalCrisis crisis, Authentication authentication) {
        crisis.setReporterNo(currentUserService.getCurrentTeacherNo(authentication));
        mentalHealthService.addCrisis(crisis);
        return ResultVO.success();
    }

    @PutMapping("/crisis/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'COUNSELOR')")
    public ResultVO<Void> updateCrisis(@PathVariable Long id, @RequestBody MentalCrisis crisis) {
        crisis.setId(id);
        mentalHealthService.updateCrisis(crisis);
        return ResultVO.success();
    }

    // ==================== Risk Warning ====================

    @GetMapping("/risk-students")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COUNSELOR')")
    public ResultVO<List<MentalHealthRecord>> getRiskStudents(
            @RequestParam(defaultValue = "WARNING") String riskLevel,
            @RequestParam(defaultValue = "20") Integer limit) {
        return ResultVO.success(mentalHealthService.getRiskStudents(riskLevel, limit));
    }
}
