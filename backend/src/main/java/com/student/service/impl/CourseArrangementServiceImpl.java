package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.dto.CourseArrangementDTO;
import com.student.entity.CourseArrangement;
import com.student.exception.BusinessException;
import com.student.mapper.CourseArrangementMapper;
import com.student.service.CourseArrangementService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseArrangementServiceImpl extends ServiceImpl<CourseArrangementMapper, CourseArrangement> implements CourseArrangementService {

    private final CourseArrangementMapper courseArrangementMapper;

    @Override
    @Transactional
    public void addArrangement(CourseArrangementDTO dto) {
        validateConflicts(dto, null);
        CourseArrangement arrangement = new CourseArrangement();
        BeanUtils.copyProperties(dto, arrangement);
        arrangement.setEnrolledCount(0);
        arrangement.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        courseArrangementMapper.insert(arrangement);
    }

    @Override
    @Transactional
    public void updateArrangement(CourseArrangementDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("Course arrangement id is required");
        }
        CourseArrangement existing = courseArrangementMapper.selectById(dto.getId());
        if (existing == null) {
            throw new BusinessException("Course arrangement not found");
        }
        validateConflicts(dto, dto.getId());

        CourseArrangement arrangement = new CourseArrangement();
        BeanUtils.copyProperties(dto, arrangement);
        arrangement.setEnrolledCount(existing.getEnrolledCount());
        arrangement.setStatus(dto.getStatus() == null ? existing.getStatus() : dto.getStatus());
        if (arrangement.getCapacity() != null && arrangement.getEnrolledCount() != null
                && arrangement.getCapacity() < arrangement.getEnrolledCount()) {
            throw new BusinessException("Capacity cannot be less than enrolled count");
        }
        courseArrangementMapper.updateById(arrangement);
    }

    @Override
    @Transactional
    public void deleteArrangement(Long id) {
        CourseArrangement existing = courseArrangementMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("Course arrangement not found");
        }
        courseArrangementMapper.deleteById(id);
    }

    @Override
    public CourseArrangement getArrangementById(Long id) {
        return courseArrangementMapper.selectByIdWithDetail(id);
    }

    @Override
    public Page<CourseArrangement> getArrangementPage(Integer page,
                                                      Integer size,
                                                      Long courseId,
                                                      Long teacherId,
                                                      Long classId,
                                                      String semester,
                                                      Integer status) {
        Page<CourseArrangement> pageParam = new Page<>(page, size);
        return courseArrangementMapper.selectPageWithDetail(pageParam, courseId, teacherId, classId, semester, status);
    }

    @Override
    public List<CourseArrangement> getArrangementOptions(Long teacherId, Long classId, Integer status) {
        return courseArrangementMapper.selectListWithDetail(teacherId, classId, status);
    }

    private void validateConflicts(CourseArrangementDTO dto, Long excludeId) {
        if (dto.getCapacity() != null && dto.getCapacity() <= 0) {
            throw new BusinessException("Capacity must be greater than 0");
        }

        LambdaQueryWrapper<CourseArrangement> classConflict = new LambdaQueryWrapper<CourseArrangement>()
                .eq(CourseArrangement::getClassId, dto.getClassId())
                .eq(CourseArrangement::getSemester, dto.getSemester())
                .eq(CourseArrangement::getSchedule, dto.getSchedule());
        if (excludeId != null) {
            classConflict.ne(CourseArrangement::getId, excludeId);
        }
        if (courseArrangementMapper.selectCount(classConflict) > 0) {
            throw new BusinessException("Class schedule conflict detected");
        }

        LambdaQueryWrapper<CourseArrangement> teacherConflict = new LambdaQueryWrapper<CourseArrangement>()
                .eq(CourseArrangement::getTeacherId, dto.getTeacherId())
                .eq(CourseArrangement::getSemester, dto.getSemester())
                .eq(CourseArrangement::getSchedule, dto.getSchedule());
        if (excludeId != null) {
            teacherConflict.ne(CourseArrangement::getId, excludeId);
        }
        if (courseArrangementMapper.selectCount(teacherConflict) > 0) {
            throw new BusinessException("Teacher schedule conflict detected");
        }

        if (StringUtils.hasText(dto.getRoom())) {
            LambdaQueryWrapper<CourseArrangement> roomConflict = new LambdaQueryWrapper<CourseArrangement>()
                    .eq(CourseArrangement::getRoom, dto.getRoom())
                    .eq(CourseArrangement::getSemester, dto.getSemester())
                    .eq(CourseArrangement::getSchedule, dto.getSchedule());
            if (excludeId != null) {
                roomConflict.ne(CourseArrangement::getId, excludeId);
            }
            if (courseArrangementMapper.selectCount(roomConflict) > 0) {
                throw new BusinessException("Room schedule conflict detected");
            }
        }
    }
}
