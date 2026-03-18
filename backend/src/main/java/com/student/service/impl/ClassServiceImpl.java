package com.student.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.entity.Class;
import com.student.entity.College;
import com.student.entity.Major;
import com.student.exception.BusinessException;
import com.student.mapper.ClassMapper;
import com.student.mapper.CollegeMapper;
import com.student.mapper.MajorMapper;
import com.student.service.ClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Year;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ClassServiceImpl extends ServiceImpl<ClassMapper, Class> implements ClassService {

    private static final int SEGMENT_LENGTH = 4;
    private static final int RANDOM_LENGTH = 4;
    private static final int GENERATE_RETRY_LIMIT = 50;
    private static final String COLLEGE_FALLBACK = "COLX";
    private static final String MAJOR_FALLBACK = "MAJR";

    private final ClassMapper classMapper;
    private final CollegeMapper collegeMapper;
    private final MajorMapper majorMapper;

    @Override
    public Class getClassById(Long id) {
        return classMapper.selectByIdWithTeacher(id);
    }

    @Override
    public Class getClassByCode(String classCode) {
        if (!StringUtils.hasText(classCode)) {
            return null;
        }
        return classMapper.selectByCodeWithTeacher(classCode);
    }

    @Override
    public Class resolveClass(String classIdentifier) {
        if (!StringUtils.hasText(classIdentifier)) {
            return null;
        }
        Class byCode = getClassByCode(classIdentifier);
        if (byCode != null) {
            return byCode;
        }
        if (classIdentifier.chars().allMatch(Character::isDigit)) {
            return getClassById(Long.parseLong(classIdentifier));
        }
        return null;
    }

    @Override
    public Page<Class> getClassPage(Integer page, Integer size, String className, String grade, String teacherNo, String collegeCode, String majorCode) {
        Page<Class> pageParam = new Page<>(page, size);

        Year gradeYear = null;
        if (StringUtils.hasText(grade)) {
            try {
                gradeYear = Year.parse(grade);
            } catch (Exception ignored) {
                gradeYear = null;
            }
        }

        Year finalGradeYear = gradeYear;
        return classMapper.selectPageWithMajor(pageParam, className, finalGradeYear, teacherNo, collegeCode, majorCode);
    }

    @Override
    public List<Class> getClassesByTeacherNo(String teacherNo) {
        if (teacherNo == null) {
            return Collections.emptyList();
        }
        return classMapper.selectByTeacherNo(teacherNo);
    }

    @Override
    @Transactional
    public Class createClass(Class clazz) {
        if (clazz == null) {
            throw new BusinessException(400, "Class payload is required");
        }
        clazz.setClassCode(generateClassCode(clazz.getCollegeCode(), clazz.getMajorCode(), clazz.getGrade()));
        if (clazz.getStudentCount() == null) {
            clazz.setStudentCount(0);
        }
        if (clazz.getStatus() == null) {
            clazz.setStatus(1);
        }
        save(clazz);
        return classMapper.selectByCodeWithTeacher(clazz.getClassCode());
    }

    @Override
    @Transactional
    public void updateClassByCode(String classCode, Class clazz) {
        Class existing = getClassByCode(classCode);
        if (existing == null) {
            throw new BusinessException(404, "Class not found");
        }
        clazz.setId(existing.getId());
        clazz.setClassCode(existing.getClassCode());
        if (clazz.getStudentCount() == null) {
            clazz.setStudentCount(existing.getStudentCount());
        }
        if (clazz.getStatus() == null) {
            clazz.setStatus(existing.getStatus());
        }
        updateById(clazz);
    }

    @Override
    @Transactional
    public void deleteClassByCode(String classCode) {
        Class existing = getClassByCode(classCode);
        if (existing == null) {
            throw new BusinessException(404, "Class not found");
        }
        removeById(existing.getId());
    }

    @Override
    public String generateClassCode(String collegeCode, String majorCode, Year grade) {
        if (grade == null) {
            throw new BusinessException(400, "Grade is required");
        }
        College college = collegeCode == null ? null : collegeMapper.selectById(collegeCode);
        if (college == null) {
            throw new BusinessException(404, "College not found");
        }
        Major major = StringUtils.hasText(majorCode) ? majorMapper.selectById(majorCode) : null;
        if (major == null) {
            throw new BusinessException(404, "Major not found");
        }
        String prefix = normalizeSegment(college.getCollegeCode(), COLLEGE_FALLBACK)
                + normalizeSegment(major.getMajorAbbreviation(), MAJOR_FALLBACK)
                + String.format(Locale.ROOT, "%04d", grade.getValue());
        for (int i = 0; i < GENERATE_RETRY_LIMIT; i++) {
            String suffix = String.format(Locale.ROOT, "%0" + RANDOM_LENGTH + "d", ThreadLocalRandom.current().nextInt(10000));
            String candidate = prefix + suffix;
            if (classMapper.selectByClassCode(candidate) == null) {
                return candidate;
            }
        }
        throw new BusinessException(500, "Failed to generate unique class code");
    }

    private String normalizeSegment(String rawValue, String fallback) {
        String normalized = rawValue == null ? "" : rawValue.trim().replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        if (!StringUtils.hasText(normalized)) {
            normalized = fallback;
        }
        if (normalized.length() >= SEGMENT_LENGTH) {
            return normalized.substring(0, SEGMENT_LENGTH);
        }
        return normalized + "X".repeat(SEGMENT_LENGTH - normalized.length());
    }
}


