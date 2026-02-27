package com.student.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.entity.Class;
import com.student.mapper.ClassMapper;
import com.student.service.ClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Year;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassServiceImpl extends ServiceImpl<ClassMapper, Class> implements ClassService {

    private final ClassMapper classMapper;

    @Override
    public Class getClassById(Long id) {
        return classMapper.selectByIdWithTeacher(id);
    }

    @Override
    public Page<Class> getClassPage(Integer page, Integer size, String className, String grade, Long teacherId) {
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
        return lambdaQuery()
                .like(StringUtils.hasText(className), Class::getClassName, className)
                .eq(finalGradeYear != null, Class::getGrade, finalGradeYear)
                .eq(teacherId != null, Class::getTeacherId, teacherId)
                .page(pageParam);
    }

    @Override
    public List<Class> getClassesByTeacherId(Long teacherId) {
        if (teacherId == null) {
            return Collections.emptyList();
        }
        return classMapper.selectByTeacherId(teacherId);
    }
}
