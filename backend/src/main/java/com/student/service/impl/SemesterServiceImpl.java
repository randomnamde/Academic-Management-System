package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.dto.SemesterDTO;
import com.student.entity.Semester;
import com.student.entity.SysConfig;
import com.student.exception.BusinessException;
import com.student.mapper.SemesterMapper;
import com.student.mapper.SysConfigMapper;
import com.student.service.SemesterService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SemesterServiceImpl extends ServiceImpl<SemesterMapper, Semester> implements SemesterService {

    private static final String CURRENT_SEMESTER_KEY = "currentSemester";
    private static final String CURRENT_SEMESTER_DESC = "Current semester for workflow and notifications";

    private final SemesterMapper semesterMapper;
    private final SysConfigMapper sysConfigMapper;

    @Override
    public Page<Semester> getSemesterPage(Integer page, Integer size, String semesterCode, Semester.Status status) {
        Page<Semester> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Semester> query = new LambdaQueryWrapper<Semester>()
                .like(StringUtils.hasText(semesterCode), Semester::getSemesterCode, semesterCode == null ? null : semesterCode.trim())
                .eq(status != null, Semester::getStatus, status)
                .orderByDesc(Semester::getStartDate)
                .orderByDesc(Semester::getCreateTime);
        return page(pageParam, query);
    }

    @Override
    @Transactional
    public Semester addSemester(SemesterDTO dto) {
        validateDateRange(dto.getStartDate(), dto.getEndDate());
        String semesterCode = normalizeSemesterCode(dto.getSemesterCode());
        ensureSemesterCodeUnique(semesterCode, null);

        Semester semester = new Semester();
        BeanUtils.copyProperties(dto, semester);
        semester.setSemesterCode(semesterCode);
        semester.setRemark(normalizeRemark(dto.getRemark()));
        semester.setStatus(Semester.Status.PLANNED);
        semesterMapper.insert(semester);
        syncCurrentSemesterConfig();
        return semesterMapper.selectById(semester.getId());
    }

    @Override
    @Transactional
    public Semester updateSemester(Long id, SemesterDTO dto) {
        Semester existing = semesterMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "学期不存在");
        }

        validateDateRange(dto.getStartDate(), dto.getEndDate());
        String semesterCode = normalizeSemesterCode(dto.getSemesterCode());
        ensureSemesterCodeUnique(semesterCode, id);

        existing.setSemesterCode(semesterCode);
        existing.setStartDate(dto.getStartDate());
        existing.setEndDate(dto.getEndDate());
        existing.setRemark(normalizeRemark(dto.getRemark()));
        semesterMapper.updateById(existing);
        syncCurrentSemesterConfig();
        return semesterMapper.selectById(id);
    }

    @Override
    @Transactional
    public Semester updateSemesterStatus(Long id, Semester.Status status) {
        Semester semester = semesterMapper.selectById(id);
        if (semester == null) {
            throw new BusinessException(404, "学期不存在");
        }
        if (status == null) {
            throw new BusinessException(400, "学期状态不能为空");
        }

        if (status == Semester.Status.ACTIVE) {
            lambdaUpdate()
                    .eq(Semester::getStatus, Semester.Status.ACTIVE)
                    .ne(Semester::getId, id)
                    .set(Semester::getStatus, Semester.Status.ENDED)
                    .update();
        }

        semester.setStatus(status);
        semesterMapper.updateById(semester);
        syncCurrentSemesterConfig();
        return semesterMapper.selectById(id);
    }

    @Override
    public List<Semester> getSemesterOptions() {
        return lambdaQuery()
                .ne(Semester::getStatus, Semester.Status.ARCHIVED)
                .orderByAsc(Semester::getStatus)
                .orderByDesc(Semester::getStartDate)
                .list();
    }

    @Override
    public Semester getActiveSemester() {
        return lambdaQuery()
                .eq(Semester::getStatus, Semester.Status.ACTIVE)
                .orderByDesc(Semester::getUpdateTime)
                .last("LIMIT 1")
                .one();
    }

    @Override
    @Transactional
    public Semester activateSemesterByCode(String semesterCode) {
        String normalizedCode = normalizeSemesterCode(semesterCode);
        Semester semester = lambdaQuery()
                .eq(Semester::getSemesterCode, normalizedCode)
                .last("LIMIT 1")
                .one();
        if (semester == null) {
            throw new BusinessException(404, "学期不存在，请先在学期管理中创建");
        }
        return updateSemesterStatus(semester.getId(), Semester.Status.ACTIVE);
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException(400, "学期开始日期不能晚于结束日期");
        }
    }

    private void ensureSemesterCodeUnique(String semesterCode, Long excludeId) {
        LambdaQueryWrapper<Semester> query = new LambdaQueryWrapper<Semester>()
                .eq(Semester::getSemesterCode, semesterCode);
        if (excludeId != null) {
            query.ne(Semester::getId, excludeId);
        }
        if (semesterMapper.selectCount(query) > 0) {
            throw new BusinessException(400, "学期编码已存在");
        }
    }

    private String normalizeSemesterCode(String semesterCode) {
        if (!StringUtils.hasText(semesterCode)) {
            throw new BusinessException(400, "请输入学期编码");
        }
        return semesterCode.trim();
    }

    private String normalizeRemark(String remark) {
        return StringUtils.hasText(remark) ? remark.trim() : null;
    }

    private void syncCurrentSemesterConfig() {
        Semester activeSemester = getActiveSemester();
        String value = activeSemester == null ? "" : activeSemester.getSemesterCode();
        SysConfig config = sysConfigMapper.selectById(CURRENT_SEMESTER_KEY);
        if (config == null) {
            config = new SysConfig();
            config.setConfigKey(CURRENT_SEMESTER_KEY);
            config.setConfigValue(value);
            config.setDescription(CURRENT_SEMESTER_DESC);
            sysConfigMapper.insert(config);
            return;
        }
        config.setConfigValue(value);
        sysConfigMapper.updateById(config);
    }
}
