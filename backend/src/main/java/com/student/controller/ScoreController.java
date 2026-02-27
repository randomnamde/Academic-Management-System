package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.ScoreDTO;
import com.student.dto.ScoreQueryDTO;
import com.student.dto.ScoreStatisticsDTO;
import com.student.entity.Score;
import com.student.service.ScoreService;
import com.student.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/score")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<Void> add(@RequestBody @Validated ScoreDTO scoreDTO) {
        scoreService.addScore(scoreDTO);
        return ResultVO.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<Void> update(@PathVariable Long id, @RequestBody @Validated ScoreDTO scoreDTO) {
        scoreDTO.setId(id);
        scoreService.updateScore(scoreDTO);
        return ResultVO.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<Void> delete(@PathVariable Long id) {
        scoreService.deleteScore(id);
        return ResultVO.success();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<Score> getById(@PathVariable Long id) {
        Score score = scoreService.getScoreById(id);
        return ResultVO.success(score);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<Page<Score>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseArrangementId,
            @RequestParam(required = false) String semester) {
        ScoreQueryDTO queryDTO = new ScoreQueryDTO();
        queryDTO.setStudentId(studentId);
        queryDTO.setCourseArrangementId(courseArrangementId);
        queryDTO.setSemester(semester);
        Page<Score> result = scoreService.getScorePage(page, size, queryDTO);
        return ResultVO.success(result);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<List<Score>> getByStudentId(@PathVariable Long studentId) {
        List<Score> scores = scoreService.getScoresByStudentId(studentId);
        return ResultVO.success(scores);
    }

    @GetMapping("/course/{courseArrangementId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<List<Score>> getByCourseArrangementId(@PathVariable Long courseArrangementId) {
        List<Score> scores = scoreService.getScoresByCourseArrangementId(courseArrangementId);
        return ResultVO.success(scores);
    }

    @GetMapping("/statistics/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<ScoreStatisticsDTO> getStatistics(@PathVariable Long studentId) {
        ScoreStatisticsDTO statistics = scoreService.getStudentStatistics(studentId);
        return ResultVO.success(statistics);
    }

    @GetMapping("/distribution/{courseArrangementId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<List<Map<String, Object>>> getDistribution(@PathVariable Long courseArrangementId) {
        List<Map<String, Object>> distribution = scoreService.getScoreDistribution(courseArrangementId);
        return ResultVO.success(distribution);
    }

    @GetMapping("/rank")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<List<Map<String, Object>>> getClassRank(
            @RequestParam Long classId,
            @RequestParam String semester) {
        List<Map<String, Object>> rank = scoreService.getClassRank(classId, semester);
        return ResultVO.success(rank);
    }

    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<Void> batchAdd(@RequestBody List<ScoreDTO> scoreDTOList) {
        scoreService.batchAddScores(scoreDTOList);
        return ResultVO.success();
    }
}
