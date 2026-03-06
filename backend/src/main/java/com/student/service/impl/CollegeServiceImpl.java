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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        Page<College> result = page(pageParam, query);
        populateAdminUsernames(result.getRecords());
        return result;
    }

    @Override
    @Transactional
    public College createCollege(CollegeDTO dto) {
        if (collegeMapper.selectByCollegeCode(dto.getCollegeCode()) != null) {
            throw new BusinessException(400, "学院编码已存在");
        }
        College college = new College();
        BeanUtils.copyProperties(dto, college);
        if (college.getStatus() == null) {
            college.setStatus(1);
        }
        save(college);
        if (StringUtils.hasText(dto.getAdminUsername())) {
            bindAdmin(college.getId(), dto.getAdminUsername(), null);
            college.setAdminUsername(dto.getAdminUsername().trim());
        }
        return college;
    }

    @Override
    @Transactional
    public void updateCollege(Long id, CollegeDTO dto, Long scopedCollegeId) {
        College existing = getById(id);
        if (existing == null) {
            throw new BusinessException(404, "学院不存在");
        }
        assertScope(existing, scopedCollegeId);
        if (StringUtils.hasText(dto.getCollegeCode())
                && !dto.getCollegeCode().equals(existing.getCollegeCode())
                && collegeMapper.selectByCollegeCode(dto.getCollegeCode()) != null) {
            throw new BusinessException(400, "学院编码已存在");
        }
        College update = new College();
        BeanUtils.copyProperties(dto, update);
        update.setId(id);
        if (update.getStatus() == null) {
            update.setStatus(existing.getStatus());
        }
        updateById(update);
        if (StringUtils.hasText(dto.getAdminUsername())) {
            bindAdmin(id, dto.getAdminUsername(), scopedCollegeId);
        }
    }

    @Override
    @Transactional
    public void updateCollegeStatus(Long id, Integer status, Long scopedCollegeId) {
        College existing = getById(id);
        if (existing == null) {
            throw new BusinessException(404, "学院不存在");
        }
        assertScope(existing, scopedCollegeId);
        College update = new College();
        update.setId(id);
        update.setStatus(status);
        updateById(update);
    }

    @Override
    @Transactional
    public void bindAdmin(Long id, String adminUsername, Long scopedCollegeId) {
        College existing = getById(id);
        if (existing == null) {
            throw new BusinessException(404, "学院不存在");
        }
        assertScope(existing, scopedCollegeId);
        if (!StringUtils.hasText(adminUsername)) {
            throw new BusinessException(400, "请输入管理员账号");
        }

        String normalizedUsername = adminUsername.trim();
        SysUser user = sysUserService.getByUsername(normalizedUsername);
        if (user == null) {
            throw new BusinessException(404, "管理员账号不存在");
        }

        College occupied = collegeMapper.selectByAdminUserId(user.getId());
        if (occupied != null && !occupied.getId().equals(id)) {
            throw new BusinessException(400, "该账号已绑定其他学院");
        }

        College update = new College();
        update.setId(id);
        update.setAdminUserId(user.getId());
        updateById(update);

        sysUserService.grantRole(user.getId(), RoleCode.COLLEGE_ADMIN);
    }

    private void assertScope(College college, Long scopedCollegeId) {
        if (scopedCollegeId != null && !scopedCollegeId.equals(college.getId())) {
            throw new BusinessException(403, "无权操作当前学院");
        }
    }

    private void populateAdminUsernames(List<College> colleges) {
        if (colleges == null || colleges.isEmpty()) {
            return;
        }

        Set<Long> adminUserIds = colleges.stream()
                .map(College::getAdminUserId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        if (adminUserIds.isEmpty()) {
            return;
        }

        Map<Long, String> usernameById = sysUserMapper.selectBatchIds(adminUserIds).stream()
                .filter(user -> user.getId() != null)
                .collect(Collectors.toMap(SysUser::getId, SysUser::getUsername, (left, right) -> left));
        for (College college : colleges) {
            college.setAdminUsername(usernameById.get(college.getAdminUserId()));
        }
    }

}
