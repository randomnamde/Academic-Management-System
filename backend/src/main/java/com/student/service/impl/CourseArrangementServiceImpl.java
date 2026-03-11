package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.dto.CourseArrangementDTO;
import com.student.entity.Class;
import com.student.entity.College;
import com.student.entity.Course;
import com.student.entity.CourseArrangement;
import com.student.entity.Teacher;
import com.student.exception.BusinessException;
import com.student.mapper.ClassMapper;
import com.student.mapper.CollegeMapper;
import com.student.mapper.CourseMapper;
import com.student.mapper.CourseArrangementMapper;
import com.student.mapper.TeacherMapper;
import com.student.service.CourseArrangementService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Year;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class CourseArrangementServiceImpl extends ServiceImpl<CourseArrangementMapper, CourseArrangement> implements CourseArrangementService {

    private final CourseArrangementMapper courseArrangementMapper;
    private final CourseMapper courseMapper;
    private final TeacherMapper teacherMapper;
    private final ClassMapper classMapper;
    private final CollegeMapper collegeMapper;

    @Override
    @Transactional
    public void addArrangement(CourseArrangementDTO dto) {
        ValidationContext validationContext = validateAndResolveContext(dto);
        validateConflicts(dto, null);
        CourseArrangement arrangement = new CourseArrangement();
        BeanUtils.copyProperties(dto, arrangement);
        arrangement.setArrangementCode(generateArrangementCode(validationContext.college(), validationContext.clazz()));
        arrangement.setEnrolledCount(0);
        arrangement.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        courseArrangementMapper.insert(arrangement);
    }

    @Override
    @Transactional
    public void updateArrangement(CourseArrangementDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("排课ID不能为空");
        }
        CourseArrangement existing = courseArrangementMapper.selectById(dto.getId());
        if (existing == null) {
            throw new BusinessException("排课不存在");
        }
        validateAndResolveContext(dto);
        validateConflicts(dto, dto.getId());

        CourseArrangement arrangement = new CourseArrangement();
        BeanUtils.copyProperties(dto, arrangement);
        arrangement.setArrangementCode(existing.getArrangementCode());
        arrangement.setEnrolledCount(existing.getEnrolledCount());
        arrangement.setStatus(dto.getStatus() == null ? existing.getStatus() : dto.getStatus());
        if (arrangement.getCapacity() != null && arrangement.getEnrolledCount() != null
                && arrangement.getCapacity() < arrangement.getEnrolledCount()) {
            throw new BusinessException("容量不能小于已选人数");
        }
        courseArrangementMapper.updateById(arrangement);
    }

    @Override
    @Transactional
    public void deleteArrangement(Long id) {
        CourseArrangement existing = courseArrangementMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("排课不存在");
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
                                                      Long collegeId,
                                                      Long courseId,
                                                      Long teacherId,
                                                      String classId,
                                                      String semester,
                                                      Integer status) {
        Page<CourseArrangement> pageParam = new Page<>(page, size);
        return courseArrangementMapper.selectPageWithDetail(pageParam, collegeId, courseId, teacherId, classId, semester, status);
    }

    @Override
    public List<CourseArrangement> getArrangementOptions(Long teacherId, String classId, Integer status) {
        return courseArrangementMapper.selectListWithDetail(teacherId, classId, status);
    }

    private void validateConflicts(CourseArrangementDTO dto, Long excludeId) {
        if (dto.getCapacity() != null && dto.getCapacity() <= 0) {
            throw new BusinessException("容量必须大于0");
        }

        LambdaQueryWrapper<CourseArrangement> classConflict = new LambdaQueryWrapper<CourseArrangement>()
                .eq(CourseArrangement::getClassId, dto.getClassId())
                .eq(CourseArrangement::getSemester, dto.getSemester())
                .eq(CourseArrangement::getSchedule, dto.getSchedule());
        if (excludeId != null) {
            classConflict.ne(CourseArrangement::getId, excludeId);
        }
        if (courseArrangementMapper.selectCount(classConflict) > 0) {
            throw new BusinessException("班级时间安排冲突");
        }

        LambdaQueryWrapper<CourseArrangement> teacherConflict = new LambdaQueryWrapper<CourseArrangement>()
                .eq(CourseArrangement::getTeacherId, dto.getTeacherId())
                .eq(CourseArrangement::getSemester, dto.getSemester())
                .eq(CourseArrangement::getSchedule, dto.getSchedule());
        if (excludeId != null) {
            teacherConflict.ne(CourseArrangement::getId, excludeId);
        }
        if (courseArrangementMapper.selectCount(teacherConflict) > 0) {
            throw new BusinessException("教师时间安排冲突");
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
                throw new BusinessException("教室时间安排冲突");
            }
        }
    }

    private ValidationContext validateAndResolveContext(CourseArrangementDTO dto) {
        Course course = courseMapper.selectById(dto.getCourseId());
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }

        Teacher teacher = teacherMapper.selectById(dto.getTeacherId());
        if (teacher == null) {
            throw new BusinessException(404, "教师不存在");
        }

        Class clazz = resolveClass(dto.getClassId());
        if (clazz == null) {
            throw new BusinessException(404, "班级不存在");
        }

        College college = collegeMapper.selectById(dto.getCollegeId());
        if (college == null) {
            throw new BusinessException(404, "学院不存在");
        }

        if (!college.getId().equals(clazz.getCollegeId())) {
            throw new BusinessException(400, "所选班级不属于当前学院");
        }
        if (!college.getId().equals(teacher.getCollegeId())) {
            throw new BusinessException(400, "所选教师不属于当前学院");
        }
        return new ValidationContext(course, teacher, clazz, college);
    }

    private String generateArrangementCode(College college, Class clazz) {
        String prefix = "C" + Year.now().getValue()
                + normalizeSegment(college.getCollegeCode())
                + normalizeSegment(clazz.getClassCode());
        int start = ThreadLocalRandom.current().nextInt(100);
        for (int offset = 0; offset < 100; offset++) {
            String candidate = prefix + String.format("%02d", (start + offset) % 100);
            long count = lambdaQuery().eq(CourseArrangement::getArrangementCode, candidate).count();
            if (count == 0) {
                return candidate;
            }
        }
        throw new BusinessException(400, "当前学院/班级下本年度排课编号已用尽");
    }

    private String normalizeSegment(String rawCode) {
        String normalized = rawCode == null ? "" : rawCode.trim().replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        if (normalized.length() >= 4) {
            return normalized.substring(normalized.length() - 4);
        }
        return "0".repeat(4 - normalized.length()) + normalized;
    }

    private record ValidationContext(Course course, Teacher teacher, Class clazz, College college) {
    }

    private Class resolveClass(String classId) {
        if (!StringUtils.hasText(classId)) {
            return null;
        }
        Class clazz = classMapper.selectByClassCode(classId);
        if (clazz != null) {
            return clazz;
        }
        if (classId.chars().allMatch(Character::isDigit)) {
            return classMapper.selectById(Long.parseLong(classId));
        }
        return null;
    }
}
