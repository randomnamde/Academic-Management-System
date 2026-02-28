package com.student.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.entity.Announcement;
import com.student.entity.SysUser;
import com.student.exception.BusinessException;
import com.student.mapper.AnnouncementMapper;
import com.student.service.AnnouncementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    @Override
    public Page<Announcement> getAnnouncementPage(Integer page,
                                                  Integer size,
                                                  String title,
                                                  Announcement.Type type,
                                                  Announcement.TargetRole targetRole,
                                                  Integer status) {
        Page<Announcement> pageParam = new Page<>(page, size);
        return lambdaQuery()
                .like(StringUtils.hasText(title), Announcement::getTitle, title)
                .eq(type != null, Announcement::getType, type)
                .eq(targetRole != null, Announcement::getTargetRole, targetRole)
                .eq(status != null, Announcement::getStatus, status)
                .orderByDesc(Announcement::getIsTop)
                .orderByDesc(Announcement::getCreateTime)
                .page(pageParam);
    }

    @Override
    public Page<Announcement> getVisibleAnnouncementPage(Integer page,
                                                         Integer size,
                                                         String title,
                                                         Announcement.Type type,
                                                         SysUser.Role role,
                                                         Long classId) {
        Page<Announcement> pageParam = new Page<>(page, size);
        Announcement.TargetRole roleTarget = Announcement.TargetRole.valueOf(role.name());
        LocalDateTime now = LocalDateTime.now();

        return lambdaQuery()
                .like(StringUtils.hasText(title), Announcement::getTitle, title)
                .eq(type != null, Announcement::getType, type)
                .eq(Announcement::getStatus, 1)
                .and(w -> w.eq(Announcement::getTargetRole, Announcement.TargetRole.ALL)
                           .or()
                           .eq(Announcement::getTargetRole, roleTarget))
                .and(w -> w.isNull(Announcement::getTargetClassId)
                           .or()
                           .eq(classId != null, Announcement::getTargetClassId, classId))
                .and(w -> w.isNull(Announcement::getStartTime)
                           .or()
                           .le(Announcement::getStartTime, now))
                .and(w -> w.isNull(Announcement::getEndTime)
                           .or()
                           .ge(Announcement::getEndTime, now))
                .orderByDesc(Announcement::getIsTop)
                .orderByDesc(Announcement::getCreateTime)
                .page(pageParam);
    }

    @Override
    @Transactional
    public void createAnnouncement(Announcement announcement) {
        if (!StringUtils.hasText(announcement.getTitle())) {
            throw new BusinessException("Title cannot be empty");
        }
        if (!StringUtils.hasText(announcement.getContent())) {
            throw new BusinessException("Content cannot be empty");
        }
        validatePublishWindow(announcement.getStartTime(), announcement.getEndTime());
        if (announcement.getType() == null) {
            announcement.setType(Announcement.Type.NOTICE);
        }
        if (announcement.getTargetRole() == null) {
            announcement.setTargetRole(Announcement.TargetRole.ALL);
        }
        if (announcement.getStatus() == null) {
            announcement.setStatus(1);
        }
        if (announcement.getPriority() == null) {
            announcement.setPriority(0);
        }
        if (announcement.getIsTop() == null) {
            announcement.setIsTop(0);
        }
        if (announcement.getViewCount() == null) {
            announcement.setViewCount(0);
        }
        if (announcement.getAuthorId() == null) {
            throw new BusinessException("Author is required");
        }
        save(announcement);
    }

    @Override
    @Transactional
    public void updateAnnouncement(Announcement announcement) {
        if (announcement.getId() == null) {
            throw new BusinessException("Announcement ID cannot be null");
        }
        if (getById(announcement.getId()) == null) {
            throw new BusinessException("Announcement not found");
        }
        validatePublishWindow(announcement.getStartTime(), announcement.getEndTime());
        updateById(announcement);
    }

    @Override
    @Transactional
    public void updateAnnouncementStatus(Long id, Integer status) {
        Announcement existing = getById(id);
        if (existing == null) {
            throw new BusinessException("Announcement not found");
        }
        existing.setStatus(status);
        updateById(existing);
    }

    private void validatePublishWindow(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new BusinessException("Start time cannot be after end time");
        }
    }
}
