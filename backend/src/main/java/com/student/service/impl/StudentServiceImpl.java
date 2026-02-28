package com.student.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.dto.StudentDTO;
import com.student.entity.Class;
import com.student.entity.Student;
import com.student.entity.SysUser;
import com.student.exception.BusinessException;
import com.student.mapper.ClassMapper;
import com.student.mapper.StudentMapper;
import com.student.mapper.SysUserMapper;
import com.student.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    private final StudentMapper studentMapper;
    private final SysUserMapper userMapper;
    private final ClassMapper classMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void addStudent(StudentDTO studentDTO) {
        if (studentMapper.selectByStudentNo(studentDTO.getStudentNo()) != null) {
            throw new BusinessException("Student number already exists");
        }
        if (!StringUtils.hasText(studentDTO.getPassword())) {
            throw new BusinessException("Password is required for new student");
        }

        SysUser user = new SysUser();
        user.setUsername(studentDTO.getStudentNo());
        user.setPassword(passwordEncoder.encode(studentDTO.getPassword()));
        user.setRealName(studentDTO.getName());
        user.setPhone(studentDTO.getPhone());
        user.setEmail(studentDTO.getEmail());
        user.setRole(SysUser.Role.STUDENT);
        user.setStatus(1);
        userMapper.insert(user);

        Student student = new Student();
        BeanUtils.copyProperties(studentDTO, student);
        student.setUserId(user.getId());
        student.setStatus(Student.Status.ENROLLED);
        studentMapper.insert(student);

        if (student.getClassId() != null) {
            updateClassStudentCount(student.getClassId());
        }
    }

    @Override
    @Transactional
    public void updateStudent(StudentDTO studentDTO) {
        if (studentDTO.getId() == null) {
            throw new BusinessException("Student ID cannot be null");
        }

        Student existStudent = studentMapper.selectById(studentDTO.getId());
        if (existStudent == null) {
            throw new BusinessException("Student not found");
        }

        Student existByNo = studentMapper.selectByStudentNo(studentDTO.getStudentNo());
        if (existByNo != null && !existByNo.getId().equals(studentDTO.getId())) {
            throw new BusinessException("Student number already used");
        }

        Long oldClassId = existStudent.getClassId();

        Student student = new Student();
        BeanUtils.copyProperties(studentDTO, student);
        studentMapper.updateById(student);

        SysUser user = new SysUser();
        user.setId(existStudent.getUserId());
        user.setUsername(studentDTO.getStudentNo());
        user.setRealName(studentDTO.getName());
        user.setPhone(studentDTO.getPhone());
        user.setEmail(studentDTO.getEmail());
        if (StringUtils.hasText(studentDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(studentDTO.getPassword()));
        }
        userMapper.updateById(user);

        if (!Objects.equals(studentDTO.getClassId(), oldClassId)) {
            updateClassStudentCount(oldClassId);
            updateClassStudentCount(studentDTO.getClassId());
        }
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new BusinessException("Student not found");
        }

        studentMapper.deleteById(id);
        userMapper.deleteById(student.getUserId());

        if (student.getClassId() != null) {
            updateClassStudentCount(student.getClassId());
        }
    }

    @Override
    public Student getStudentById(Long id) {
        return studentMapper.selectByIdWithClass(id);
    }

    @Override
    public Page<Student> getStudentPage(Integer page, Integer size, String studentNo, String name, Long classId, Student.Status status) {
        Page<Student> pageParam = new Page<>(page, size);
        return studentMapper.selectPageWithClass(pageParam, studentNo, name, classId, status);
    }

    @Override
    public List<Student> getStudentsByClassId(Long classId) {
        return studentMapper.selectByClassId(classId);
    }

    @Override
    @Transactional
    public void updateStudentStatus(Long id, Student.Status status) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new BusinessException("Student not found");
        }
        studentMapper.updateStatus(id, status);
    }

    @Override
    public Student getStudentByUserId(Long userId) {
        return lambdaQuery().eq(Student::getUserId, userId).one();
    }

    @Override
    public Map<String, Long> getGenderStatistics() {
        Map<String, Long> result = new HashMap<>();
        result.put("male", 0L);
        result.put("female", 0L);

        List<Map<String, Object>> rows = studentMapper.countByGender();
        for (Map<String, Object> row : rows) {
            Object gender = row.get("gender");
            Object count = row.get("count");
            if (!(count instanceof Number)) {
                continue;
            }

            long value = ((Number) count).longValue();
            if (gender == Student.Gender.MALE || "MALE".equalsIgnoreCase(String.valueOf(gender))) {
                result.put("male", value);
            } else if (gender == Student.Gender.FEMALE || "FEMALE".equalsIgnoreCase(String.valueOf(gender))) {
                result.put("female", value);
            }
        }

        return result;
    }

    private void updateClassStudentCount(Long classId) {
        if (classId == null) {
            return;
        }
        Long count = studentMapper.countByClassId(classId);
        Class clazz = new Class();
        clazz.setId(classId);
        clazz.setStudentCount(count == null ? 0 : count.intValue());
        classMapper.updateById(clazz);
    }
}
