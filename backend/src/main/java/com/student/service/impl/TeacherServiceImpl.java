package com.student.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.dto.TeacherDTO;
import com.student.entity.SysUser;
import com.student.entity.Teacher;
import com.student.exception.BusinessException;
import com.student.mapper.SysUserMapper;
import com.student.mapper.TeacherMapper;
import com.student.security.RoleCode;
import com.student.service.TeacherService;
import com.student.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {

    private final TeacherMapper teacherMapper;
    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SysUserService sysUserService;

    @Override
    @Transactional
    public void addTeacher(TeacherDTO teacherDTO) {
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
        user.setRole(SysUser.Role.TEACHER);
        user.setStatus(1);
        userMapper.insert(user);
        sysUserService.grantRole(user.getId(), RoleCode.COURSE_TEACHER);

        Teacher teacher = new Teacher();
        BeanUtils.copyProperties(teacherDTO, teacher);
        teacher.setUserId(user.getId());
        teacher.setStatus(1);
        teacherMapper.insert(teacher);
    }

    @Override
    @Transactional
    public void updateTeacher(TeacherDTO teacherDTO) {
        if (teacherDTO.getId() == null) {
            throw new BusinessException("Teacher ID cannot be null");
        }

        Teacher existing = teacherMapper.selectById(teacherDTO.getId());
        if (existing == null) {
            throw new BusinessException("Teacher not found");
        }

        Teacher existingByNo = teacherMapper.selectByTeacherNo(teacherDTO.getTeacherNo());
        if (existingByNo != null && !existingByNo.getId().equals(teacherDTO.getId())) {
            throw new BusinessException("Teacher number already used");
        }

        Teacher teacher = new Teacher();
        BeanUtils.copyProperties(teacherDTO, teacher);
        teacher.setId(existing.getId());
        teacher.setUserId(existing.getUserId());
        teacherMapper.updateById(teacher);

        SysUser user = new SysUser();
        user.setId(existing.getUserId());
        user.setUsername(teacherDTO.getTeacherNo());
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
    public void deleteTeacher(Long id) {
        Teacher existing = teacherMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("Teacher not found");
        }

        teacherMapper.deleteById(id);
        userMapper.deleteById(existing.getUserId());
    }

    @Override
    public Teacher getTeacherById(Long id) {
        return teacherMapper.selectById(id);
    }

    @Override
    public Page<Teacher> getTeacherPage(Integer page, Integer size, String teacherNo, String name, Long departmentId) {
        Page<Teacher> pageParam = new Page<>(page, size);
        return lambdaQuery()
                .like(StringUtils.hasText(teacherNo), Teacher::getTeacherNo, teacherNo)
                .like(StringUtils.hasText(name), Teacher::getName, name)
                .page(pageParam);
    }

    @Override
    @Transactional
    public void updateTeacherStatus(Long id, Integer status) {
        Teacher existing = teacherMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("Teacher not found");
        }

        Teacher teacher = new Teacher();
        teacher.setId(id);
        teacher.setStatus(status);
        teacherMapper.updateById(teacher);

        SysUser user = new SysUser();
        user.setId(existing.getUserId());
        user.setStatus(status);
        userMapper.updateById(user);
    }
}
