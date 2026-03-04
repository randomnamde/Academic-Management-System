package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.dto.CollegeDTO;
import com.student.entity.College;
import com.student.entity.SysUser;
import com.student.exception.BusinessException;
import com.student.mapper.CollegeMapper;
import com.student.mapper.SysUserMapper;
import com.student.security.RoleCode;
import com.student.service.CollegeService;
import com.student.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CollegeServiceImpl extends ServiceImpl<CollegeMapper, College> implements CollegeService {

    private final CollegeMapper collegeMapper;
    private final SysUserMapper sysUserMapper;
    private final SysUserService sysUserService;

    @Override
    public Page<College> getCollegePage(Integer page, Integer size, String keyword, Integer status, Long scopedCollegeId) {
        Page<College> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<College> query = new LambdaQueryWrapper<College>()
                .eq(scopedCollegeId != null, College::getId, scopedCollegeId)
                .eq(status != null, College::getStatus, status)
                .orderByDesc(College::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper
                    .like(College::getCollegeName, keyword)
                    .or()
                    .like(College::getCollegeCode, keyword));
        }
        return page(pageParam, query);
    }

    @Override
    @Transactional
    public College createCollege(CollegeDTO dto) {
        if (collegeMapper.selectByCollegeCode(dto.getCollegeCode()) != null) {
            throw new BusinessException("College code already exists");
        }
        College college = new College();
        BeanUtils.copyProperties(dto, college);
        if (college.getStatus() == null) {
            college.setStatus(1);
        }
        save(college);
        if (dto.getAdminUserId() != null) {
            bindAdmin(college.getId(), dto.getAdminUserId(), null);
        }
        return college;
    }

    @Override
    @Transactional
    public void updateCollege(Long id, CollegeDTO dto, Long scopedCollegeId) {
        College existing = getById(id);
        if (existing == null) {
            throw new BusinessException("College not found");
        }
        assertScope(existing, scopedCollegeId);
        if (StringUtils.hasText(dto.getCollegeCode())
                && !dto.getCollegeCode().equals(existing.getCollegeCode())
                && collegeMapper.selectByCollegeCode(dto.getCollegeCode()) != null) {
            throw new BusinessException("College code already exists");
        }
        College update = new College();
        BeanUtils.copyProperties(dto, update);
        update.setId(id);
        if (update.getStatus() == null) {
            update.setStatus(existing.getStatus());
        }
        updateById(update);
        if (dto.getAdminUserId() != null) {
            bindAdmin(id, dto.getAdminUserId(), scopedCollegeId);
        }
    }

    @Override
    @Transactional
    public void updateCollegeStatus(Long id, Integer status, Long scopedCollegeId) {
        College existing = getById(id);
        if (existing == null) {
            throw new BusinessException("College not found");
        }
        assertScope(existing, scopedCollegeId);
        College update = new College();
        update.setId(id);
        update.setStatus(status);
        updateById(update);
    }

    @Override
    @Transactional
    public void bindAdmin(Long id, Long adminUserId, Long scopedCollegeId) {
        College existing = getById(id);
        if (existing == null) {
            throw new BusinessException("College not found");
        }
        assertScope(existing, scopedCollegeId);
        SysUser user = sysUserMapper.selectById(adminUserId);
        if (user == null) {
            throw new BusinessException("Admin user not found");
        }

        College occupied = collegeMapper.selectByAdminUserId(adminUserId);
        if (occupied != null && !occupied.getId().equals(id)) {
            throw new BusinessException("This user is already bound to another college");
        }

        College update = new College();
        update.setId(id);
        update.setAdminUserId(adminUserId);
        updateById(update);

        sysUserService.grantRole(adminUserId, RoleCode.COLLEGE_ADMIN);
    }

    private void assertScope(College college, Long scopedCollegeId) {
        if (scopedCollegeId != null && !scopedCollegeId.equals(college.getId())) {
            throw new BusinessException(403, "Forbidden");
        }
    }

}
