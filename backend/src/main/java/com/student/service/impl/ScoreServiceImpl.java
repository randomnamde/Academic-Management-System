package com.student.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.dto.ScoreDTO;
import com.student.dto.ScoreQueryDTO;
import com.student.dto.ScoreStatisticsDTO;
import com.student.entity.Score;
import com.student.exception.BusinessException;
import com.student.mapper.ScoreMapper;
import com.student.mapper.StudentMapper;
import com.student.service.ScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScoreServiceImpl extends ServiceImpl<ScoreMapper, Score> implements ScoreService {

    private static final BigDecimal PASS_SCORE = new BigDecimal("60.00");

    private final ScoreMapper scoreMapper;
    private final StudentMapper studentMapper;

    @Override
    @Transactional
    public void addScore(ScoreDTO scoreDTO) {
        Score existScore = scoreMapper.selectByStudentAndCourse(scoreDTO.getStudentId(), scoreDTO.getCourseArrangementId());
        if (existScore != null) {
            throw new BusinessException("Score for this student and course arrangement already exists");
        }

        Score score = new Score();
        BeanUtils.copyProperties(scoreDTO, score);
        score.calculateTotalScore();
        applyPersistedStatusRule(score);

        scoreMapper.insert(score);
    }

    @Override
    @Transactional
    public void updateScore(ScoreDTO scoreDTO) {
        if (scoreDTO.getId() == null) {
            throw new BusinessException("Score id cannot be null");
        }

        Score existScore = scoreMapper.selectById(scoreDTO.getId());
        if (existScore == null) {
            throw new BusinessException("Score not found");
        }

        Score score = new Score();
        BeanUtils.copyProperties(scoreDTO, score);
        score.calculateTotalScore();
        applyPersistedStatusRule(score);

        scoreMapper.updateById(score);
    }

    @Override
    @Transactional
    public void deleteScore(Long id) {
        Score score = scoreMapper.selectById(id);
        if (score == null) {
            throw new BusinessException("Score not found");
        }

        scoreMapper.deleteById(id);
    }

    @Override
    public Score getScoreById(Long id) {
        return scoreMapper.selectByIdWithDetail(id);
    }

    @Override
    public Page<Score> getScorePage(Integer page, Integer size, ScoreQueryDTO queryDTO) {
        Page<Score> pageParam = new Page<>(page, size);
        return scoreMapper.selectPageWithDetail(
                pageParam,
                queryDTO.getStudentId(),
                queryDTO.getTeacherId(),
                queryDTO.getCourseArrangementId(),
                queryDTO.getSemester(),
                queryDTO.getCollegeId(),
                queryDTO.getClassId());
    }

    @Override
    public List<Score> getScoresByStudentId(Long studentId) {
        return scoreMapper.selectByStudentId(studentId);
    }

    @Override
    public List<Score> getScoresByCourseArrangementId(Long courseArrangementId) {
        return scoreMapper.selectByCourseArrangementId(courseArrangementId);
    }

    @Override
    public ScoreStatisticsDTO getStudentStatistics(Long studentId) {
        ScoreStatisticsDTO statistics = new ScoreStatisticsDTO();

        var student = studentMapper.selectByIdWithClass(studentId);
        if (student != null) {
            statistics.setStudentId(studentId);
            statistics.setStudentName(student.getName());
            statistics.setStudentNo(student.getStudentNo());
            if (student.getClassName() != null) {
                statistics.setClassName(student.getClassName());
            }
        }

        Double avgScore = scoreMapper.selectAverageScoreByStudent(studentId);
        statistics.setAverageScore(avgScore != null ? avgScore : 0.0);

        Long totalCourses = scoreMapper.countTotalByStudent(studentId);
        statistics.setTotalCourses(totalCourses.intValue());

        Long passedCourses = scoreMapper.countPassedByStudent(studentId);
        statistics.setPassedCourses(passedCourses.intValue());
        statistics.setFailedCourses((int) (totalCourses - passedCourses));

        List<Score> scores = scoreMapper.selectByStudentId(studentId);
        if (!scores.isEmpty()) {
            BigDecimal totalGpa = scores.stream()
                    .filter(s -> s.getGpa() != null)
                    .map(Score::getGpa)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal avgGpa = totalGpa.divide(new BigDecimal(scores.size()), 2, RoundingMode.HALF_UP);
            statistics.setGpa(avgGpa);
        } else {
            statistics.setGpa(BigDecimal.ZERO);
        }

        return statistics;
    }

    @Override
    public List<Map<String, Object>> getScoreDistribution(Long courseArrangementId) {
        return scoreMapper.selectScoreDistribution(courseArrangementId);
    }

    @Override
    public List<Map<String, Object>> getClassRank(String classId, String semester) {
        return scoreMapper.selectClassRank(classId, semester);
    }

    @Override
    @Transactional
    public void batchAddScores(List<ScoreDTO> scoreDTOList) {
        for (ScoreDTO scoreDTO : scoreDTOList) {
            addScore(scoreDTO);
        }
    }

    private void applyPersistedStatusRule(Score score) {
        if (score == null) return;
        if (score.getTotalScore() != null && score.getTotalScore().compareTo(PASS_SCORE) < 0) {
            score.setStatus(Score.Status.MAKEUP);
            return;
        }
        score.setStatus(Score.Status.NORMAL);
    }
}
