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
import com.student.mapper.CourseArrangementMapper;
import com.student.mapper.CourseMapper;
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
        ValidationContext context = validateAndResolveContext(dto);
        validateConflicts(dto, null);

        CourseArrangement arrangement = new CourseArrangement();
        BeanUtils.copyProperties(dto, arrangement);
        arrangement.setArrangementCode(generateArrangementCode(context.college(), context.clazz()));
        arrangement.setEnrolledCount(0);
        arrangement.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        courseArrangementMapper.insert(arrangement);
    }

    @Override
    @Transactional
    public void updateArrangement(CourseArrangementDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("Arrangement id is required");
        }
        CourseArrangement existing = courseArrangementMapper.selectById(dto.getId());
        if (existing == null) {
            throw new BusinessException("Course arrangement not found");
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
            throw new BusinessException("Capacity cannot be smaller than enrolled count");
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
                                                      String collegeCode,
                                                      String courseCode,
                                                      String teacherNo,
                                                      String classId,
                                                      String semester,
                                                      Integer status) {
        Page<CourseArrangement> pageParam = new Page<>(page, size);
        return courseArrangementMapper.selectPageWithDetail(pageParam, collegeCode, courseCode, teacherNo, classId, semester, status);
    }

    @Override
    public List<CourseArrangement> getArrangementOptions(String teacherNo, String classId, Integer status) {
        return courseArrangementMapper.selectListWithDetail(teacherNo, classId, status);
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
            throw new BusinessException("Class schedule conflict");
        }

        LambdaQueryWrapper<CourseArrangement> teacherConflict = new LambdaQueryWrapper<CourseArrangement>()
                .eq(CourseArrangement::getTeacherNo, dto.getTeacherNo())
                .eq(CourseArrangement::getSemester, dto.getSemester())
                .eq(CourseArrangement::getSchedule, dto.getSchedule());
        if (excludeId != null) {
            teacherConflict.ne(CourseArrangement::getId, excludeId);
        }
        if (courseArrangementMapper.selectCount(teacherConflict) > 0) {
            throw new BusinessException("Teacher schedule conflict");
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
                throw new BusinessException("Room schedule conflict");
            }
        }
    }

    private ValidationContext validateAndResolveContext(CourseArrangementDTO dto) {
        Course course = courseMapper.selectById(dto.getCourseCode());
        if (course == null) {
            throw new BusinessException(404, "Course not found");
        }

        Teacher teacher = teacherMapper.selectById(dto.getTeacherNo());
        if (teacher == null) {
            throw new BusinessException(404, "Teacher not found");
        }

        Class clazz = resolveClass(dto.getClassId());
        if (clazz == null) {
            throw new BusinessException(404, "Class not found");
        }

        College college = collegeMapper.selectById(dto.getCollegeCode());
        if (college == null) {
            throw new BusinessException(404, "College not found");
        }

        if (!college.getCollegeCode().equals(clazz.getCollegeCode())) {
            throw new BusinessException(400, "Class does not belong to the selected college");
        }
        if (!college.getCollegeCode().equals(teacher.getCollegeCode())) {
            throw new BusinessException(400, "Teacher does not belong to the selected college");
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
        throw new BusinessException(400, "Unable to generate arrangement code");
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