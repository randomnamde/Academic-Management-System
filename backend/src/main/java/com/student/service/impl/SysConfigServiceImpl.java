package com.student.service.impl;

import com.student.entity.SysConfig;
import com.student.entity.Semester;
import com.student.exception.BusinessException;
import com.student.mapper.SemesterMapper;
import com.student.mapper.SysConfigMapper;
import com.student.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl implements SysConfigService {

    public static final String KEY_CURRENT_SEMESTER = "currentSemester";

    private final SysConfigMapper sysConfigMapper;
    private final SemesterMapper semesterMapper;

    @Override
    public String getCurrentSemester() {
        Semester activeSemester = semesterMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Semester>()
                        .eq(Semester::getStatus, Semester.Status.ACTIVE)
                        .orderByDesc(Semester::getUpdateTime)
                        .last("LIMIT 1"));
        if (activeSemester == null || !StringUtils.hasText(activeSemester.getSemesterCode())) {
            return "";
        }
        return activeSemester.getSemesterCode();
    }

    @Override
    @Transactional
    public void setCurrentSemester(String semester) {
        if (!StringUtils.hasText(semester)) {
            throw new BusinessException(400, "请输入学期编码");
        }
        Semester target = semesterMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Semester>()
                        .eq(Semester::getSemesterCode, semester.trim())
                        .last("LIMIT 1"));
        if (target == null) {
            throw new BusinessException(404, "学期不存在，请先在学期管理中创建");
        }

        semesterMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Semester>()
                .eq(Semester::getStatus, Semester.Status.ACTIVE)
                .ne(Semester::getId, target.getId())
                .set(Semester::getStatus, Semester.Status.ENDED));
        target.setStatus(Semester.Status.ACTIVE);
        semesterMapper.updateById(target);

        SysConfig existing = sysConfigMapper.selectById(KEY_CURRENT_SEMESTER);
        if (existing == null) {
            SysConfig config = new SysConfig();
            config.setConfigKey(KEY_CURRENT_SEMESTER);
            config.setConfigValue(target.getSemesterCode());
            config.setDescription("Current semester for workflow and notifications");
            sysConfigMapper.insert(config);
            return;
        }
        existing.setConfigValue(target.getSemesterCode());
        sysConfigMapper.updateById(existing);
    }
}
