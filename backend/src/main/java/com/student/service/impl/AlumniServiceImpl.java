package com.student.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.AlumniDTO;
import com.student.entity.Alumni;
import com.student.exception.BusinessException;
import com.student.mapper.AlumniMapper;
import com.student.service.AlumniService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AlumniServiceImpl implements AlumniService {

    @Autowired
    private AlumniMapper alumniMapper;

    @Override
    public Page<Alumni> getAlumniPage(Integer page, Integer size, String studentId, String name,
                                        Integer graduationYear, String collegeCode, String majorCode) {
        int offset = (page - 1) * size;
        List<Alumni> records = alumniMapper.selectPageWithDetail(offset, size, studentId,
                name, graduationYear, collegeCode, majorCode);
        Long total = alumniMapper.countWithDetail(studentId, name, graduationYear, collegeCode, majorCode);

        Page<Alumni> result = new Page<>(page, size);
        result.setRecords(records);
        result.setTotal(total);
        return result;
    }

    @Override
    @Transactional
    public Alumni createAlumni(AlumniDTO dto) {
        Alumni existing = alumniMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Alumni>()
                        .eq("student_id", dto.getStudentId())
        );
        if (existing != null) {
            throw new BusinessException(400, "该学生校友信息已存在");
        }

        Alumni alumni = new Alumni();
        BeanUtils.copyProperties(dto, alumni);
        alumni.setStatus(1);
        alumniMapper.insert(alumni);
        return alumni;
    }

    @Override
    @Transactional
    public Alumni updateAlumni(Long id, AlumniDTO dto) {
        Alumni existing = alumniMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "校友信息不存在");
        }

        BeanUtils.copyProperties(dto, existing, "id", "createTime");
        alumniMapper.updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public void deleteAlumni(Long id) {
        alumniMapper.deleteById(id);
    }

    @Override
    public Alumni getByStudentId(String studentId) {
        return alumniMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Alumni>()
                        .eq("student_id", studentId)
        );
    }
}
