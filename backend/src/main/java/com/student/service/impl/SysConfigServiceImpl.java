package com.student.service.impl;

import com.student.entity.SysConfig;
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

    @Override
    public String getCurrentSemester() {
        SysConfig config = sysConfigMapper.selectById(KEY_CURRENT_SEMESTER);
        if (config == null || !StringUtils.hasText(config.getConfigValue())) {
            return "2025-2026-2";
        }
        return config.getConfigValue();
    }

    @Override
    @Transactional
    public void setCurrentSemester(String semester) {
        SysConfig existing = sysConfigMapper.selectById(KEY_CURRENT_SEMESTER);
        if (existing == null) {
            SysConfig config = new SysConfig();
            config.setConfigKey(KEY_CURRENT_SEMESTER);
            config.setConfigValue(semester);
            config.setDescription("Current semester for workflow and notifications");
            sysConfigMapper.insert(config);
            return;
        }
        existing.setConfigValue(semester);
        sysConfigMapper.updateById(existing);
    }
}

