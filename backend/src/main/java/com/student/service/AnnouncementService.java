package com.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.entity.Announcement;

public interface AnnouncementService extends IService<Announcement> {

    Page<Announcement> getAnnouncementPage(Integer page,
                                           Integer size,
                                           String title,
                                           Announcement.Type type,
                                           Announcement.TargetRole targetRole,
                                           Integer status);

    void createAnnouncement(Announcement announcement);

    void updateAnnouncement(Announcement announcement);

    void updateAnnouncementStatus(Long id, Integer status);
}
