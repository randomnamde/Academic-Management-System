package com.student.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.dto.TeacherDTO;
import com.student.entity.College;
import com.student.entity.SysUser;
import com.student.entity.Teacher;
import com.student.exception.BusinessException;
import com.student.mapper.CollegeMapper;
import com.student.mapper.SysUserMapper;
import com.student.mapper.TeacherMapper;
import com.student.security.RoleCode;
import com.student.service.SysUserService;
import com.student.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Year;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {

    private static final int RETRY_LIMIT = 50;

    private final TeacherMapper teacherMapper;
    private final SysUserMapper userMapper;
    private final CollegeMapper collegeMapper;
    private final PasswordEncoder passwordEncoder;
    private final SysUserService sysUserService;

    @Override
    @Transactional
    public void addTeacher(TeacherDTO teacherDTO) {
        if (!StringUtils.hasText(teacherDTO.getCollegeCode())) {
            throw new BusinessException("College is required");
        }
        if (!StringUtils.hasText(teacherDTO.getTeacherNo())) {
            teacherDTO.setTeacherNo(generateNextTeacherNo(teacherDTO.getCollegeCode()));
        }
        if (teacherMapper.selectByTeacherNo(teacherDTO.getTeacherNo()) != null) {
            throw new BusinessException("Teacher number already exists");
        }
        if (userMapper.selectByUsername(teacherDTO.getTeacherNo()) != null) {
            throw new BusinessException("Username already exists");
        }
        if (!StringUtils.hasText(teacherDTO.getPassword())) {
            throw new BusinessException("Password is required for new teacher");
        }

        SysUser user = new SysUser();
        user.setUsername(teacherDTO.getTeacherNo());
        user.setPassword(passwordEncoder.encode(teacherDTO.getPassword()));
        user.setRealName(teacherDTO.getName());
        user.setPhone(teacherDTO.getPhone());
        user.setEmail(teacherDTO.getEmail());
        user.setRole(SysUser.Role.COURSE_TEACHER);
        user.setStatus(1);
        userMapper.insert(user);
        sysUserService.grantRole(user.getUsername(), RoleCode.COURSE_TEACHER);

        Teacher teacher = new Teacher();
        BeanUtils.copyProperties(teacherDTO, teacher);
        teacher.setUserId(user.getUsername());
        teacher.setStatus(1);
        teacherMapper.insert(teacher);
    }

    @Override
    @Transactional
    public void updateTeacher(TeacherDTO teacherDTO) {
        if (!StringUtils.hasText(teacherDTO.getTeacherNo())) {
            throw new BusinessException("Teacher number cannot be null");
        }

        Teacher existing = teacherMapper.selectByTeacherNo(teacherDTO.getTeacherNo());
        if (existing == null) {
            throw new BusinessException("Teacher not found");
        }

        Teacher teacher = new Teacher();
        BeanUtils.copyProperties(teacherDTO, teacher);
        teacher.setTeacherNo(existing.getTeacherNo());
        teacher.setId(existing.getId());
        teacher.setUserId(existing.getUserId());
        teacherMapper.updateById(teacher);

        SysUser user = new SysUser();
        user.setUsername(existing.getUserId());
        user.setRealName(teacherDTO.getName());
        user.setPhone(teacherDTO.getPhone());
        user.setEmail(teacherDTO.getEmail());
        if (StringUtils.hasText(teacherDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(teacherDTO.getPassword()));
        }
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void deleteTeacher(String teacherNo) {
        Teacher existing = teacherMapper.selectByTeacherNo(teacherNo);
        if (existing == null) {
            throw new BusinessException("Teacher not found");
        }

        teacherMapper.deleteById(existing.getTeacherNo());
        userMapper.deleteById(existing.getUserId());
    }

    @Override
    public Teacher getTeacherByNo(String teacherNo) {
        return teacherMapper.selectByTeacherNo(teacherNo);
    }

    @Override
    public Page<Teacher> getTeacherPage(Integer page, Integer size, String teacherNo, String name, String department) {
        Page<Teacher> pageParam = new Page<>(page, size);
        return lambdaQuery()
                .like(StringUtils.hasText(teacherNo), Teacher::getTeacherNo, teacherNo)
                .like(StringUtils.hasText(name), Teacher::getName, name)
                .like(StringUtils.hasText(department), Teacher::getDepartment, department)
                .page(pageParam);
    }

    @Override
    @Transactional
    public void updateTeacherStatus(String teacherNo, Integer status) {
        Teacher existing = teacherMapper.selectByTeacherNo(teacherNo);
        if (existing == null) {
            throw new BusinessException("Teacher not found");
        }

        Teacher teacher = new Teacher();
        teacher.setTeacherNo(existing.getTeacherNo());
        teacher.setStatus(status);
        teacherMapper.updateById(teacher);

        SysUser user = new SysUser();
        user.setUsername(existing.getUserId());
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public String generateNextTeacherNo(String collegeCode) {
        return generateTeacherNo(collegeCode);
    }

    public String generateTeacherNo(String collegeCode) {
        if (!StringUtils.hasText(collegeCode)) {
            throw new BusinessException("College is required for teacher number generation");
        }
        College college = collegeMapper.selectById(collegeCode);
        if (college == null || !StringUtils.hasText(college.getCollegeCode())) {
            throw new BusinessException("College not found");
        }
        String suffix = normalizeCollegeCode(college.getCollegeCode());
        String year = String.valueOf(Year.now().getValue());
        for (int i = 0; i < RETRY_LIMIT; i++) {
            String teacherNo = "T" + suffix + year + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
            if (teacherMapper.selectByTeacherNo(teacherNo) == null && userMapper.selectByUsername(teacherNo) == null) {
                return teacherNo;
            }
        }
        throw new BusinessException("Failed to generate unique teacher number");
    }

    private String normalizeCollegeCode(String rawCode) {
        String normalized = rawCode.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException("College code cannot be empty");
        }
        if (normalized.length() > 4) {
            return normalized.substring(0, 4);
        }
        if (normalized.length() < 4) {
            return "0".repeat(4 - normalized.length()) + normalized;
        }
        return normalized;
    }
}