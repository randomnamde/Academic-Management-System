package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.ScoreDTO;
import com.student.dto.ScoreQueryDTO;
import com.student.dto.ScoreStatisticsDTO;
import com.student.entity.CourseArrangement;
import com.student.entity.Score;
import com.student.mapper.CourseArrangementMapper;
import com.student.security.CurrentUserService;
import com.student.security.DataScopeService;
import com.student.service.ScoreService;
import com.student.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/score")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;
    private final CurrentUserService currentUserService;
    private final DataScopeService dataScopeService;
    private final CourseArrangementMapper courseArrangementMapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<Void> add(@RequestBody @Validated ScoreDTO scoreDTO, Authentication authentication) {
        assertCollegeAdminArrangementScope(authentication, scoreDTO.getCourseArrangementId());
        dataScopeService.assertTeacherOwnsArrangement(authentication, scoreDTO.getCourseArrangementId());
        scoreService.addScore(scoreDTO);
        return ResultVO.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<Void> update(@PathVariable Long id,
                                 @RequestBody @Validated ScoreDTO scoreDTO,
                                 Authentication authentication) {
        Score existing = scoreService.getScoreById(id);
        if (existing == null) {
            return ResultVO.error(404, "Score not found");
        }
        assertCollegeAdminArrangementScope(authentication, existing.getCourseArrangementId());
        assertCollegeAdminArrangementScope(authentication, scoreDTO.getCourseArrangementId());
        dataScopeService.assertTeacherOwnsArrangement(authentication, existing.getCourseArrangementId());
        dataScopeService.assertTeacherOwnsArrangement(authentication, scoreDTO.getCourseArrangementId());
        scoreDTO.setId(id);
        scoreService.updateScore(scoreDTO);
        return ResultVO.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<Void> delete(@PathVariable Long id, Authentication authentication) {
        Score existing = scoreService.getScoreById(id);
        if (existing == null) {
            return ResultVO.error(404, "Score not found");
        }
        assertCollegeAdminArrangementScope(authentication, existing.getCourseArrangementId());
        dataScopeService.assertTeacherOwnsArrangement(authentication, existing.getCourseArrangementId());
        scoreService.deleteScore(id);
        return ResultVO.success();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<Score> getById(@PathVariable Long id, Authentication authentication) {
        Score score = scoreService.getScoreById(id);
        if (score == null) {
            return ResultVO.error(404, "Score not found");
        }
        assertCollegeAdminArrangementScope(authentication, score.getCourseArrangementId());
        if (currentUserService.isStudent(authentication)) {
            Long studentId = currentUserService.getCurrentStudentId(authentication);
            if (!studentId.equals(score.getStudentId())) {
                return ResultVO.error(403, "Forbidden");
            }
        }
        dataScopeService.assertTeacherOwnsArrangement(authentication, score.getCourseArrangementId());
        return ResultVO.success(score);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<Page<Score>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseArrangementId,
            @RequestParam(required = false) String semester,
            Authentication authentication) {
        assertCollegeAdminArrangementScope(authentication, courseArrangementId);
        Long scopedStudentId = dataScopeService.resolveScopedStudentId(authentication, studentId);
        Long scopedTeacherId = dataScopeService.resolveScopedTeacherId(authentication, null);
        dataScopeService.assertTeacherOwnsArrangement(authentication, courseArrangementId);
        ScoreQueryDTO queryDTO = new ScoreQueryDTO();
        queryDTO.setStudentId(scopedStudentId);
        queryDTO.setTeacherId(scopedTeacherId);
        queryDTO.setCourseArrangementId(courseArrangementId);
        queryDTO.setSemester(semester);
        Page<Score> result = scoreService.getScorePage(page, size, queryDTO);
        return ResultVO.success(result);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<List<Score>> getByStudentId(@PathVariable Long studentId, Authentication authentication) {
        Long scopedStudentId = dataScopeService.resolveScopedStudentId(authentication, studentId);
        ScoreQueryDTO queryDTO = new ScoreQueryDTO();
        queryDTO.setStudentId(scopedStudentId);
        queryDTO.setTeacherId(dataScopeService.resolveScopedTeacherId(authentication, null));
        Page<Score> scorePage = scoreService.getScorePage(1, 10000, queryDTO);
        return ResultVO.success(scorePage.getRecords());
    }

    @GetMapping("/course/{courseArrangementId}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<List<Score>> getByCourseArrangementId(@PathVariable Long courseArrangementId, Authentication authentication) {
        assertCollegeAdminArrangementScope(authentication, courseArrangementId);
        dataScopeService.assertTeacherOwnsArrangement(authentication, courseArrangementId);
        List<Score> scores = scoreService.getScoresByCourseArrangementId(courseArrangementId);
        return ResultVO.success(scores);
    }

    @GetMapping("/statistics/{studentId}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<ScoreStatisticsDTO> getStatistics(@PathVariable Long studentId, Authentication authentication) {
        if (currentUserService.isStudent(authentication)) {
            studentId = currentUserService.getCurrentStudentId(authentication);
        } else if (currentUserService.isTeacher(authentication)) {
            ScoreQueryDTO queryDTO = new ScoreQueryDTO();
            queryDTO.setStudentId(studentId);
            queryDTO.setTeacherId(dataScopeService.resolveCurrentTeacherId(authentication));
            Page<Score> scopedPage = scoreService.getScorePage(1, 10000, queryDTO);
            List<Score> scopedScores = scopedPage.getRecords();
            if (scopedScores.isEmpty()) {
                return ResultVO.error(403, "Forbidden");
            }
            return ResultVO.success(buildScopedStatistics(studentId, scopedScores));
        }
        ScoreStatisticsDTO statistics = scoreService.getStudentStatistics(studentId);
        return ResultVO.success(statistics);
    }

    @GetMapping("/distribution/{courseArrangementId}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<List<Map<String, Object>>> getDistribution(@PathVariable Long courseArrangementId, Authentication authentication) {
        assertCollegeAdminArrangementScope(authentication, courseArrangementId);
        dataScopeService.assertTeacherOwnsArrangement(authentication, courseArrangementId);
        List<Map<String, Object>> distribution = scoreService.getScoreDistribution(courseArrangementId);
        return ResultVO.success(distribution);
    }

    @GetMapping("/rank")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<List<Map<String, Object>>> getClassRank(
            @RequestParam Long classId,
            @RequestParam String semester) {
        List<Map<String, Object>> rank = scoreService.getClassRank(classId, semester);
        return ResultVO.success(rank);
    }

    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER')")
    public ResultVO<Void> batchAdd(@RequestBody List<ScoreDTO> scoreDTOList, Authentication authentication) {
        for (ScoreDTO item : scoreDTOList) {
            assertCollegeAdminArrangementScope(authentication, item.getCourseArrangementId());
        }
        dataScopeService.assertTeacherOwnsArrangements(
                authentication,
                scoreDTOList.stream().map(ScoreDTO::getCourseArrangementId).toList());
        scoreService.batchAddScores(scoreDTOList);
        return ResultVO.success();
    }

    private void assertCollegeAdminArrangementScope(Authentication authentication, Long arrangementId) {
        Long scopedCollegeId = dataScopeService.resolveScopedCollegeId(authentication);
        if (scopedCollegeId == null || arrangementId == null) {
            return;
        }
        CourseArrangement arrangement = courseArrangementMapper.selectById(arrangementId);
        if (arrangement == null) {
            return;
        }
        java.util.Set<Long> classIds = dataScopeService.resolveCollegeClassIds(authentication);
        if (!classIds.contains(arrangement.getClassId())) {
            throw new com.student.exception.BusinessException(403, "Forbidden");
        }
    }

    private ScoreStatisticsDTO buildScopedStatistics(Long studentId, List<Score> scopedScores) {
        ScoreStatisticsDTO dto = new ScoreStatisticsDTO();
        dto.setStudentId(studentId);
        dto.setStudentName(scopedScores.get(0).getStudentName());
        dto.setStudentNo(scopedScores.get(0).getStudentNo());
        dto.setTotalCourses(scopedScores.size());

        long passed = scopedScores.stream()
                .filter(item -> item.getTotalScore() != null && item.getTotalScore().compareTo(new BigDecimal("60")) >= 0)
                .count();
        dto.setPassedCourses((int) passed);
        dto.setFailedCourses(scopedScores.size() - (int) passed);

        double averageScore = scopedScores.stream()
                .map(Score::getTotalScore)
                .filter(item -> item != null)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0D);
        dto.setAverageScore(BigDecimal.valueOf(averageScore).setScale(2, RoundingMode.HALF_UP).doubleValue());

        BigDecimal totalGpa = scopedScores.stream()
                .map(Score::getGpa)
                .filter(item -> item != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgGpa = scopedScores.isEmpty()
                ? BigDecimal.ZERO
                : totalGpa.divide(BigDecimal.valueOf(scopedScores.size()), 2, RoundingMode.HALF_UP);
        dto.setGpa(avgGpa);
        return dto;
    }
}

