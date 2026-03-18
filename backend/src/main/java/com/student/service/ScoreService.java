package com.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.dto.ScoreDTO;
import com.student.dto.ScoreQueryDTO;
import com.student.dto.ScoreStatisticsDTO;
import com.student.entity.Score;

import java.util.List;
import java.util.Map;

public interface ScoreService extends IService<Score> {

    void addScore(ScoreDTO scoreDTO);

    void updateScore(ScoreDTO scoreDTO);

    void deleteScore(Long id);

    Score getScoreById(Long id);

    Page<Score> getScorePage(Integer page, Integer size, ScoreQueryDTO queryDTO);

    List<Score> getScoresByStudentId(String studentId);

    List<Score> getScoresByCourseArrangementId(Long courseArrangementId);

    ScoreStatisticsDTO getStudentStatistics(String studentId);

    List<Map<String, Object>> getScoreDistribution(Long courseArrangementId);

    List<Map<String, Object>> getClassRank(String classCode, String semester);

    void batchAddScores(List<ScoreDTO> scoreDTOList);
}
