package com.student.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.dto.MajorDTO;
import com.student.entity.Major;
import com.student.exception.BusinessException;
import com.student.mapper.CollegeMapper;
import com.student.mapper.MajorMapper;
import com.student.service.MajorService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class MajorServiceImpl extends ServiceImpl<MajorMapper, Major> implements MajorService {

    private static final int RANDOM_CODE_LENGTH = 4;
    private static final int PREFIX_LENGTH = 4;
    private static final int GENERATE_RETRY_LIMIT = 30;
    private static final String DEFAULT_PREFIX = "GENX";

    private final MajorMapper majorMapper;
    private final CollegeMapper collegeMapper;

    @Override
    public Page<Major> getMajorPage(Integer page, Integer size, String keyword, Long collegeId, Integer status, Long scopedCollegeId) {
        Long effectiveCollegeId = scopedCollegeId != null ? scopedCollegeId : collegeId;
        return majorMapper.selectPageWithCollege(new Page<>(page, size), keyword, effectiveCollegeId, status);
    }

    @Override
    public List<Major> getMajorOptions(Long collegeId, Integer status, Long scopedCollegeId) {
        Long effectiveCollegeId = scopedCollegeId != null ? scopedCollegeId : collegeId;
        return majorMapper.selectOptions(effectiveCollegeId, status);
    }

    @Override
    public Major getMajorDetail(String majorCode, Long scopedCollegeId) {
        Major major = majorMapper.selectByCodeWithCollege(majorCode);
        if (major == null) {
            return null;
        }
        assertCollegeScope(major.getCollegeId(), scopedCollegeId);
        return major;
    }

    @Override
    @Transactional
    public Major createMajor(MajorDTO dto, Long scopedCollegeId) {
        Long collegeId = resolveScopedCollegeId(dto.getCollegeId(), scopedCollegeId);
        assertCollegeExists(collegeId);

        Major major = new Major();
        BeanUtils.copyProperties(dto, major);
        major.setCollegeId(collegeId);
        major.setMajorAbbreviation(normalizeAbbreviation(dto.getMajorAbbreviation(), dto.getMajorName()));
        major.setMajorCode(generateMajorCode(dto.getMajorName(), major.getMajorAbbreviation()));
        if (major.getStatus() == null) {
            major.setStatus(1);
        }
        save(major);
        return majorMapper.selectByCodeWithCollege(major.getMajorCode());
    }

    @Override
    @Transactional
    public void updateMajor(String majorCode, MajorDTO dto, Long scopedCollegeId) {
        Major existing = majorMapper.selectByCodeWithCollege(majorCode);
        if (existing == null) {
            throw new BusinessException(404, "Major not found");
        }
        assertCollegeScope(existing.getCollegeId(), scopedCollegeId);

        Long collegeId = resolveScopedCollegeId(dto.getCollegeId(), scopedCollegeId);
        assertCollegeExists(collegeId);

        Major update = new Major();
        update.setMajorCode(majorCode);
        update.setMajorName(dto.getMajorName());
        update.setMajorAbbreviation(normalizeAbbreviation(dto.getMajorAbbreviation(), dto.getMajorName()));
        update.setCollegeId(collegeId);
        update.setDescription(dto.getDescription());
        update.setStatus(dto.getStatus() == null ? existing.getStatus() : dto.getStatus());
        updateById(update);
    }

    @Override
    @Transactional
    public void updateMajorStatus(String majorCode, Integer status, Long scopedCollegeId) {
        Major existing = majorMapper.selectByCodeWithCollege(majorCode);
        if (existing == null) {
            throw new BusinessException(404, "Major not found");
        }
        assertCollegeScope(existing.getCollegeId(), scopedCollegeId);
        Major update = new Major();
        update.setMajorCode(majorCode);
        update.setStatus(status);
        updateById(update);
    }

    @Override
    public String generateMajorCode(String majorName, String majorAbbreviation) {
        String prefix = normalizeAbbreviation(majorAbbreviation, majorName);
        for (int i = 0; i < GENERATE_RETRY_LIMIT; i++) {
            String suffix = String.format(Locale.ROOT, "%0" + RANDOM_CODE_LENGTH + "d", ThreadLocalRandom.current().nextInt(10000));
            String candidate = prefix + suffix;
            if (majorMapper.selectById(candidate) == null) {
                return candidate;
            }
        }
        throw new BusinessException(500, "Failed to generate unique major code");
    }

    private String normalizeAbbreviation(String majorAbbreviation, String majorName) {
        String cleaned = keepLetters(majorAbbreviation);
        if (!StringUtils.hasText(cleaned)) {
            cleaned = deriveAbbreviationFromName(majorName);
        }
        if (!StringUtils.hasText(cleaned)) {
            cleaned = DEFAULT_PREFIX;
        }
        if (cleaned.length() >= PREFIX_LENGTH) {
            return cleaned.substring(0, PREFIX_LENGTH);
        }
        return (cleaned + DEFAULT_PREFIX).substring(0, PREFIX_LENGTH);
    }

    private String deriveAbbreviationFromName(String majorName) {
        if (!StringUtils.hasText(majorName)) {
            return DEFAULT_PREFIX;
        }
        String[] parts = majorName.trim().split("[\\s\\-_/]+");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            String letters = keepLetters(part);
            if (StringUtils.hasText(letters)) {
                initials.append(letters.charAt(0));
            }
        }
        if (initials.length() >= PREFIX_LENGTH) {
            return initials.substring(0, PREFIX_LENGTH);
        }
        String directLetters = keepLetters(majorName);
        if (directLetters.length() >= PREFIX_LENGTH) {
            return directLetters.substring(0, PREFIX_LENGTH);
        }
        if (StringUtils.hasText(directLetters)) {
            return directLetters;
        }
        return DEFAULT_PREFIX;
    }

    private String keepLetters(String input) {
        if (!StringUtils.hasText(input)) {
            return "";
        }
        return input.replaceAll("[^A-Za-z]", "").toUpperCase(Locale.ROOT);
    }

    private Long resolveScopedCollegeId(Long collegeId, Long scopedCollegeId) {
        if (scopedCollegeId != null) {
            return scopedCollegeId;
        }
        if (collegeId == null) {
            throw new BusinessException(400, "College is required");
        }
        return collegeId;
    }

    private void assertCollegeExists(Long collegeId) {
        if (collegeId == null || collegeMapper.selectById(collegeId) == null) {
            throw new BusinessException(404, "College not found");
        }
    }

    private void assertCollegeScope(Long collegeId, Long scopedCollegeId) {
        if (scopedCollegeId != null && !scopedCollegeId.equals(collegeId)) {
            throw new BusinessException(403, "Forbidden");
        }
    }
}
