package com.student.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl implements SysConfigService {

    public static final String KEY_CURRENT_SEMESTER = "currentSemester";
    public static final String KEY_COURSE_TIME_SLOTS = "courseTimeSlots";
    private static final int MAX_COURSE_TIME_SLOTS = 12;
    private static final List<String> DEFAULT_COURSE_TIME_SLOTS = List.of(
            "08:00-09:40",
            "10:00-11:40",
            "14:00-15:40",
            "16:00-17:40",
            "19:00-20:40"
    );

    private final SysConfigMapper sysConfigMapper;
    private final SemesterMapper semesterMapper;
    private final ObjectMapper objectMapper;

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

    @Override
    public List<String> getCourseTimeSlots() {
        SysConfig existing = sysConfigMapper.selectById(KEY_COURSE_TIME_SLOTS);
        if (existing == null || !StringUtils.hasText(existing.getConfigValue())) {
            return DEFAULT_COURSE_TIME_SLOTS;
        }
        try {
            List<String> parsed = objectMapper.readValue(existing.getConfigValue(), new TypeReference<List<String>>() {
            });
            List<String> normalized = normalizeCourseTimeSlots(parsed, false);
            return normalized.isEmpty() ? DEFAULT_COURSE_TIME_SLOTS : normalized;
        } catch (Exception ignored) {
            return DEFAULT_COURSE_TIME_SLOTS;
        }
    }

    @Override
    @Transactional
    public void setCourseTimeSlots(List<String> timeSlots) {
        List<String> normalized = normalizeCourseTimeSlots(timeSlots, true);
        try {
            String serialized = objectMapper.writeValueAsString(normalized);
            if (serialized.length() > 255) {
                throw new BusinessException(400, "上课时间配置过长，请减少节次数量");
            }
            upsertConfig(KEY_COURSE_TIME_SLOTS, serialized, "Configurable course time slots for arrangements and timetables");
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(500, "保存上课时间配置失败");
        }
    }

    private void upsertConfig(String configKey, String configValue, String description) {
        SysConfig existing = sysConfigMapper.selectById(configKey);
        if (existing == null) {
            SysConfig config = new SysConfig();
            config.setConfigKey(configKey);
            config.setConfigValue(configValue);
            config.setDescription(description);
            sysConfigMapper.insert(config);
            return;
        }
        existing.setConfigValue(configValue);
        existing.setDescription(description);
        sysConfigMapper.updateById(existing);
    }

    private List<String> normalizeCourseTimeSlots(List<String> timeSlots, boolean strict) {
        if (timeSlots == null || timeSlots.isEmpty()) {
            if (strict) {
                throw new BusinessException(400, "请至少保留一个上课时间段");
            }
            return List.of();
        }

        Set<String> uniqueSlots = new LinkedHashSet<>();
        for (String rawSlot : timeSlots) {
            String slot = rawSlot == null ? "" : rawSlot.trim();
            if (!StringUtils.hasText(slot)) {
                if (strict) {
                    throw new BusinessException(400, "上课时间段不能为空");
                }
                continue;
            }
            validateCourseTimeSlot(slot);
            if (!uniqueSlots.add(slot) && strict) {
                throw new BusinessException(400, "上课时间段不能重复");
            }
        }

        List<String> normalized = new ArrayList<>(uniqueSlots);
        if (normalized.isEmpty()) {
            if (strict) {
                throw new BusinessException(400, "请至少保留一个上课时间段");
            }
            return List.of();
        }
        if (normalized.size() > MAX_COURSE_TIME_SLOTS) {
            throw new BusinessException(400, "上课时间段最多配置12项");
        }
        return normalized;
    }

    private void validateCourseTimeSlot(String slot) {
        String[] parts = slot.split("-");
        if (parts.length != 2) {
            throw new BusinessException(400, "上课时间格式应为 HH:mm-HH:mm");
        }
        try {
            LocalTime start = LocalTime.parse(parts[0]);
            LocalTime end = LocalTime.parse(parts[1]);
            if (!start.isBefore(end)) {
                throw new BusinessException(400, "上课结束时间必须晚于开始时间");
            }
        } catch (DateTimeParseException ex) {
            throw new BusinessException(400, "上课时间格式应为 HH:mm-HH:mm");
        }
    }
}
