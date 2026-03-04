package com.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.entity.Announcement;
import com.student.security.RoleCode;

import java.util.Set;

public interface AnnouncementService extends IService<Announcement> {

    Page<Announcement> getAnnouncementPage(Integer page,
                                           Integer size,
                                           String title,
                                           Announcement.Type type,
                                           Announcement.TargetRole targetRole,
                                           Integer status);

    Page<Announcement> getVisibleAnnouncementPage(Integer page,
                                                  Integer size,
                                                  String title,
                                                  Announcement.Type type,
                                                  Set<RoleCode> roles,
                                                  Long classId);

    void createAnnouncement(Announcement announcement);

    void updateAnnouncement(Announcement announcement);

    void updateAnnouncementStatus(Long id, Integer status);
}
