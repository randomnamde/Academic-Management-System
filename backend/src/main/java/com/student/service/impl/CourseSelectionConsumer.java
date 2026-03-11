package com.student.service.impl;

import com.student.dto.CourseSelectionRequestDTO;
import com.student.entity.StudentCourseSelection;
import com.student.mapper.CourseArrangementMapper;
import com.student.mapper.StudentCourseSelectionMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonShutdownException;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseSelectionConsumer {

    private static final String SELECTION_QUEUE = "course:selection:queue";
    private static final String COURSE_INV_PREFIX = "course:inv:";

    private final RedissonClient redissonClient;
    private final StudentCourseSelectionMapper selectionMapper;
    private final CourseArrangementMapper courseArrangementMapper;

    private volatile boolean running;
    private Thread consumerThread;

    @PostConstruct
    public void startConsumer() {
        running = true;
        consumerThread = new Thread(this::consumeLoop, "CourseSelectionConsumerThread");
        consumerThread.setDaemon(true);
        consumerThread.start();
    }

    @PreDestroy
    public void stopConsumer() {
        running = false;
        if (consumerThread != null) {
            consumerThread.interrupt();
        }
    }

    private void consumeLoop() {
        RBlockingQueue<CourseSelectionRequestDTO> queue = redissonClient.getBlockingQueue(SELECTION_QUEUE);
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                CourseSelectionRequestDTO request = queue.take();
                processSelection(request);
            } catch (InterruptedException e) {
                log.info("CourseSelectionConsumer interrupted, stopping consumer thread");
                Thread.currentThread().interrupt();
                break;
            } catch (RedissonShutdownException e) {
                log.info("Redisson is shutdown, stopping course selection consumer");
                break;
            } catch (Exception e) {
                if (!running) {
                    break;
                }
                log.error("Error processing course selection", e);
            }
        }
        log.info("CourseSelectionConsumer stopped");
    }

    @Transactional
    public void processSelection(CourseSelectionRequestDTO request) {
        String studentId = request.getStudentId();
        Long arrangementId = request.getCourseArrangementId();

        try {
            StudentCourseSelection selection = new StudentCourseSelection();
            selection.setStudentId(studentId);
            selection.setCourseArrangementId(arrangementId);
            selection.setStatus("SUCCESS");
            selectionMapper.insert(selection);

            courseArrangementMapper.updateEnrolledCount(arrangementId, 1);
            log.info("Student {} selected course {} successfully", studentId, arrangementId);
        } catch (Exception e) {
            log.error("Student {} failed to select course {}: {}", studentId, arrangementId, e.getMessage());

            String key = COURSE_INV_PREFIX + arrangementId;
            RAtomicLong stock = redissonClient.getAtomicLong(key);
            stock.incrementAndGet();

            StudentCourseSelection selection = new StudentCourseSelection();
            selection.setStudentId(studentId);
            selection.setCourseArrangementId(arrangementId);
            selection.setStatus("FAILED");
            selection.setRemark(e.getMessage());
            selectionMapper.insert(selection);
        }
    }
}
