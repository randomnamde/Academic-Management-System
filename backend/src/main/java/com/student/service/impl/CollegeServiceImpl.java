package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.dto.CollegeDTO;
import com.student.entity.College;
import com.student.entity.SysUser;
import com.student.exception.BusinessException;
import com.student.mapper.CollegeMapper;
import com.student.mapper.MajorMapper;
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
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollegeServiceImpl extends ServiceImpl<CollegeMapper, College> implements CollegeService {

    private static final int COLLEGE_CODE_LENGTH = 4;
    private static final int COLLEGE_PREFIX_LENGTH = 2;
    private static final int COLLEGE_SUFFIX_LENGTH = 2;
    private static final int COLLEGE_SUFFIX_RADIX = 36;
    private static final int COLLEGE_MAX_SUFFIX = 1295;
    private static final String COLLEGE_PREFIX_FALLBACK = "CL";
    private static final int GENERATE_RETRY_LIMIT = 50;

    private final CollegeMapper collegeMapper;
    private final MajorMapper majorMapper;
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
                    .like(College::getCollegeNameEn, keyword)
                    .or()
                    .like(College::getCollegeCode, keyword));
        }
        Page<College> result = page(pageParam, query);
        populateAdminUsernames(result.getRecords());
        populateCounts(result.getRecords());
        return result;
    }

    @Override
    @Transactional
    public College createCollege(CollegeDTO dto) {
        College college = new College();
        BeanUtils.copyProperties(dto, college);
        college.setCollegeCode(generateCollegeCode(dto.getCollegeNameEn(), dto.getCollegeName()));
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
            throw new BusinessException(404, "College not found");
        }
        assertScope(existing, scopedCollegeId);

        if (StringUtils.hasText(dto.getCollegeCode())) {
            String nextCode = dto.getCollegeCode().trim();
            if (!nextCode.equals(existing.getCollegeCode()) && collegeMapper.selectByCollegeCode(nextCode) != null) {
                throw new BusinessException(400, "College code already exists");
            }
            dto.setCollegeCode(nextCode);
        }

        College update = new College();
        BeanUtils.copyProperties(dto, update);
        update.setId(id);
        if (!StringUtils.hasText(update.getCollegeCode())) {
            update.setCollegeCode(existing.getCollegeCode());
        }
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
            throw new BusinessException(404, "College not found");
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
            throw new BusinessException(404, "College not found");
        }
        assertScope(existing, scopedCollegeId);
        if (!StringUtils.hasText(adminUsername)) {
            throw new BusinessException(400, "Please input admin username");
        }

        String normalizedUsername = adminUsername.trim();
        SysUser user = sysUserService.getByUsername(normalizedUsername);
        if (user == null) {
            throw new BusinessException(404, "Admin user not found");
        }

        College occupied = collegeMapper.selectByAdminUserId(user.getUsername());
        if (occupied != null && !occupied.getId().equals(id)) {
            throw new BusinessException(400, "This account is bound to another college");
        }

        College update = new College();
        update.setId(id);
        update.setAdminUserId(user.getUsername());
        updateById(update);

        sysUserService.grantRole(user.getUsername(), RoleCode.COLLEGE_ADMIN);
    }

    private void assertScope(College college, Long scopedCollegeId) {
        if (scopedCollegeId != null && !scopedCollegeId.equals(college.getId())) {
            throw new BusinessException(403, "Forbidden");
        }
    }

    private void populateAdminUsernames(List<College> colleges) {
        if (colleges == null || colleges.isEmpty()) {
            return;
        }

        Set<String> adminUserIds = colleges.stream()
                .map(College::getAdminUserId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        if (adminUserIds.isEmpty()) {
            return;
        }

        Map<String, String> usernameById = sysUserMapper.selectList(
                        new LambdaQueryWrapper<SysUser>().in(SysUser::getUsername, adminUserIds))
                .stream()
                .filter(user -> user.getUsername() != null)
                .collect(Collectors.toMap(SysUser::getUsername, SysUser::getUsername, (left, right) -> left));
        for (College college : colleges) {
            college.setAdminUsername(usernameById.get(college.getAdminUserId()));
        }
    }

    private void populateCounts(List<College> colleges) {
        if (colleges == null || colleges.isEmpty()) {
            return;
        }
        for (College college : colleges) {
            if (college.getId() == null) {
                continue;
            }
            college.setMajorCount(majorMapper.countByCollegeId(college.getId()));
        }
    }

    private String generateCollegeCode(String collegeNameEn, String collegeNameZh) {
        String prefix = resolveCodePrefix(collegeNameEn, collegeNameZh);
        int maxSuffixValue = -1;
        List<College> samePrefixColleges = lambdaQuery()
                .likeRight(College::getCollegeCode, prefix)
                .list();

        for (College existing : samePrefixColleges) {
            String code = existing.getCollegeCode();
            if (!StringUtils.hasText(code) || code.length() != COLLEGE_CODE_LENGTH) {
                continue;
            }
            if (!code.startsWith(prefix)) {
                continue;
            }
            String suffix = code.substring(COLLEGE_PREFIX_LENGTH);
            if (!suffix.matches("[0-9A-Z]{" + COLLEGE_SUFFIX_LENGTH + "}")) {
                continue;
            }
            int value = Integer.parseInt(suffix, COLLEGE_SUFFIX_RADIX);
            if (value > maxSuffixValue) {
                maxSuffixValue = value;
            }
        }

        int nextSuffixValue = maxSuffixValue + 1;
        if (nextSuffixValue <= COLLEGE_MAX_SUFFIX) {
            String candidate = prefix + toBase36(nextSuffixValue);
            if (collegeMapper.selectByCollegeCode(candidate) == null) {
                return candidate;
            }
        }

        for (int i = 0; i < GENERATE_RETRY_LIMIT; i++) {
            int randomValue = ThreadLocalRandom.current().nextInt(COLLEGE_MAX_SUFFIX + 1);
            String candidate = prefix + toBase36(randomValue);
            if (collegeMapper.selectByCollegeCode(candidate) == null) {
                return candidate;
            }
        }
        throw new BusinessException(500, "Failed to generate unique college code");
    }

    private String resolveCodePrefix(String collegeNameEn, String collegeNameZh) {
        String letters = keepLetters(collegeNameEn);
        if (!StringUtils.hasText(letters)) {
            letters = keepLetters(collegeNameZh);
        }
        if (!StringUtils.hasText(letters)) {
            return COLLEGE_PREFIX_FALLBACK;
        }
        if (letters.length() >= COLLEGE_PREFIX_LENGTH) {
            return letters.substring(0, COLLEGE_PREFIX_LENGTH);
        }
        return (letters + "X").substring(0, COLLEGE_PREFIX_LENGTH);
    }

    private String keepLetters(String input) {
        if (!StringUtils.hasText(input)) {
            return "";
        }
        return input.trim().replaceAll("[^A-Za-z]", "").toUpperCase(Locale.ROOT);
    }

    private String toBase36(int value) {
        String code = Integer.toString(value, COLLEGE_SUFFIX_RADIX).toUpperCase(Locale.ROOT);
        if (code.length() >= COLLEGE_SUFFIX_LENGTH) {
            return code.substring(code.length() - COLLEGE_SUFFIX_LENGTH);
        }
        return "0".repeat(COLLEGE_SUFFIX_LENGTH - code.length()) + code;
    }
}
