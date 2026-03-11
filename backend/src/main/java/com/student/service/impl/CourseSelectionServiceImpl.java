package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.dto.CourseSelectionRequestDTO;
import com.student.entity.CourseArrangement;
import com.student.entity.SelectionRound;
import com.student.entity.StudentCourseSelection;
import com.student.mapper.CourseArrangementMapper;
import com.student.mapper.SelectionRoundMapper;
import com.student.mapper.StudentCourseSelectionMapper;
import com.student.service.CourseSelectionService;
import com.student.common.Result;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseSelectionServiceImpl implements CourseSelectionService {

    private final RedissonClient redissonClient;
    private final CourseArrangementMapper courseArrangementMapper;
    private final SelectionRoundMapper selectionRoundMapper;
    private final StudentCourseSelectionMapper selectionMapper;

    private static final String COURSE_INV_PREFIX = "course:inv:";
    private static final String SELECTION_QUEUE = "course:selection:queue";

    @Override
    public Result<String> preloadInventory() {
        List<CourseArrangement> arrangements = courseArrangementMapper.selectList(null);
        for (CourseArrangement arrangement : arrangements) {
            String key = COURSE_INV_PREFIX + arrangement.getId();
            RAtomicLong stock = redissonClient.getAtomicLong(key);
            stock.set(arrangement.getCapacity() - arrangement.getEnrolledCount());
        }
        return Result.success("库存预热成功");
    }

    @Override
    public Result<String> selectCourse(CourseSelectionRequestDTO requestDTO) {
        // 1. 校验选课轮次是否开启
        LocalDateTime now = LocalDateTime.now();
        List<SelectionRound> activeRounds = selectionRoundMapper.selectList(new LambdaQueryWrapper<SelectionRound>()
                .eq(SelectionRound::getStatus, 1)
                .le(SelectionRound::getStartTime, now)
                .ge(SelectionRound::getEndTime, now));

        if (activeRounds.isEmpty()) {
            return Result.error("当前无活跃的选课轮次");
        }

        Long arrangementId = requestDTO.getCourseArrangementId();
        String studentId = requestDTO.getStudentId();

        // 2. Redis 原子预扣减库存
        String key = COURSE_INV_PREFIX + arrangementId;
        RAtomicLong stock = redissonClient.getAtomicLong(key);
        
        // 简单的乐观校验：如果库存 <= 0 则直接失败
        if (stock.get() <= 0) {
            return Result.error("该课程已选满");
        }

        // 原子减1
        long remaining = stock.decrementAndGet();
        if (remaining < 0) {
            // 恢复库存
            stock.incrementAndGet();
            return Result.error("该课程已选满");
        }

        // 3. 进入异步队列
        RBlockingQueue<CourseSelectionRequestDTO> queue = redissonClient.getBlockingQueue(SELECTION_QUEUE);
        queue.add(requestDTO);

        return Result.success("正在排队中，请稍后查询结果");
    }

    @Override
    public Result<String> getSelectionResult(String studentId, Long arrangementId) {
        StudentCourseSelection selection = selectionMapper.selectOne(new LambdaQueryWrapper<StudentCourseSelection>()
                .eq(StudentCourseSelection::getStudentId, studentId)
                .eq(StudentCourseSelection::getCourseArrangementId, arrangementId));

        if (selection == null) {
            return Result.success("QUEUING"); // 还在排队或处理中
        }
        return Result.success(selection.getStatus());
    }
}
