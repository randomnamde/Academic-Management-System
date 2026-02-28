package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.entity.Announcement;
import com.student.entity.Student;
import com.student.entity.SysUser;
import com.student.security.CurrentUserService;
import com.student.service.AnnouncementService;
import com.student.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final CurrentUserService currentUserService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<Page<Announcement>> list(@RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer size,
                                             @RequestParam(required = false) String title,
                                             @RequestParam(required = false) Announcement.Type type,
                                             @RequestParam(required = false) Announcement.TargetRole targetRole,
                                             @RequestParam(required = false) Integer status,
                                             Authentication authentication) {
        SysUser currentUser = currentUserService.getCurrentUser(authentication);
        if (currentUser.getRole() == SysUser.Role.STUDENT) {
            Student student = currentUserService.getCurrentStudent(authentication);
            return ResultVO.success(
                    announcementService.getVisibleAnnouncementPage(page, size, title, type, currentUser.getRole(), student.getClassId())
            );
        }
        return ResultVO.success(announcementService.getAnnouncementPage(page, size, title, type, targetRole, status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResultVO<Announcement> getById(@PathVariable Long id, Authentication authentication) {
        Announcement announcement = announcementService.getById(id);
        if (announcement == null) {
            return ResultVO.error(404, "Announcement not found");
        }

        SysUser currentUser = currentUserService.getCurrentUser(authentication);
        if (currentUser.getRole() == SysUser.Role.STUDENT) {
            Student student = currentUserService.getCurrentStudent(authentication);
            if (!isVisibleForStudent(announcement, student)) {
                return ResultVO.error(403, "Forbidden");
            }
        }
        return ResultVO.success(announcement);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<Void> add(@RequestBody Announcement announcement, Authentication authentication) {
        SysUser currentUser = currentUserService.getCurrentUser(authentication);
        announcement.setAuthorId(currentUser.getId());
        announcementService.createAnnouncement(announcement);
        return ResultVO.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<Void> update(@PathVariable Long id, @RequestBody Announcement announcement) {
        announcement.setId(id);
        announcementService.updateAnnouncement(announcement);
        return ResultVO.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<Void> delete(@PathVariable Long id) {
        announcementService.removeById(id);
        return ResultVO.success();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResultVO<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        announcementService.updateAnnouncementStatus(id, status);
        return ResultVO.success();
    }

    private boolean isVisibleForStudent(Announcement announcement, Student student) {
        if (announcement.getStatus() == null || announcement.getStatus() != 1) {
            return false;
        }

        if (!(announcement.getTargetRole() == Announcement.TargetRole.ALL
                || announcement.getTargetRole() == Announcement.TargetRole.STUDENT)) {
            return false;
        }

        if (announcement.getTargetClassId() != null
                && (student.getClassId() == null || !announcement.getTargetClassId().equals(student.getClassId()))) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (announcement.getStartTime() != null && announcement.getStartTime().isAfter(now)) {
            return false;
        }
        return announcement.getEndTime() == null || !announcement.getEndTime().isBefore(now);
    }
}
