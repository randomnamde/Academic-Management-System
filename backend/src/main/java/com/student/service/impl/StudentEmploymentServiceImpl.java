package com.student.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.StudentEmploymentDTO;
import com.student.entity.StudentEmployment;
import com.student.exception.BusinessException;
import com.student.mapper.StudentEmploymentMapper;
import com.student.service.StudentEmploymentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Service
public class StudentEmploymentServiceImpl implements StudentEmploymentService {

    @Autowired
    private StudentEmploymentMapper employmentMapper;

    @Override
    public Page<StudentEmployment> getEmploymentPage(Integer page, Integer size, String studentId,
                                                       Integer graduationYear, String employmentStatus,
                                                       String collegeCode) {
        int offset = (page - 1) * size;
        List<StudentEmployment> records = employmentMapper.selectPageWithDetail(offset, size, studentId,
                graduationYear, employmentStatus, collegeCode);
        Long total = employmentMapper.countWithDetail(studentId, graduationYear, employmentStatus, collegeCode);

        Page<StudentEmployment> result = new Page<>(page, size);
        result.setRecords(records);
        result.setTotal(total);
        return result;
    }

    @Override
    @Transactional
    public StudentEmployment createEmployment(StudentEmploymentDTO dto) {
        StudentEmployment existing = employmentMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<StudentEmployment>()
                        .eq("student_id", dto.getStudentId())
        );
        if (existing != null) {
            throw new BusinessException(400, "该学生就业信息已存在");
        }

        StudentEmployment employment = new StudentEmployment();
        BeanUtils.copyProperties(dto, employment);
        employmentMapper.insert(employment);
        return employment;
    }

    @Override
    @Transactional
    public StudentEmployment updateEmployment(Long id, StudentEmploymentDTO dto) {
        StudentEmployment existing = employmentMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "就业信息不存在");
        }

        BeanUtils.copyProperties(dto, existing, "id", "recordTime");
        employmentMapper.updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public void deleteEmployment(Long id) {
        employmentMapper.deleteById(id);
    }

    @Override
    public StudentEmployment getByStudentId(String studentId) {
        return employmentMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<StudentEmployment>()
                        .eq("student_id", studentId)
        );
    }

    @Override
    public List<Map<String, Object>> getStatisticsByYear(Integer graduationYear, String collegeCode) {
        return employmentMapper.selectStatisticsByYear(graduationYear, collegeCode);
    }

    @Override
    public List<Map<String, Object>> getStatisticsByIndustry(Integer graduationYear, String collegeCode) {
        return employmentMapper.selectStatisticsByIndustry(graduationYear, collegeCode);
    }

    @Override
    public List<Map<String, Object>> getStatisticsByCompanyType(Integer graduationYear, String collegeCode) {
        return employmentMapper.selectStatisticsByCompanyType(graduationYear, collegeCode);
    }

    @Override
    public Map<String, Object> getEmploymentRate(Integer graduationYear, String collegeCode) {
        return employmentMapper.selectEmploymentRate(graduationYear, collegeCode);
    }
}
