package com.student.security;

import com.student.entity.CourseArrangement;
import com.student.entity.SysUser;
import com.student.exception.BusinessException;
import com.student.mapper.CourseArrangementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class DataScopeService {

    private final CurrentUserService currentUserService;
    private final CourseArrangementMapper courseArrangementMapper;

    public Long resolveScopedStudentId(Authentication authentication, Long requestedStudentId) {
        if (currentUserService.isStudent(authentication)) {
            return currentUserService.getCurrentStudentId(authentication);
        }
        return requestedStudentId;
    }

    public Long resolveScopedTeacherId(Authentication authentication, Long requestedTeacherId) {
        if (currentUserService.isTeacher(authentication)) {
            return currentUserService.getCurrentTeacherId(authentication);
        }
        return requestedTeacherId;
    }

    public Long resolveCurrentTeacherId(Authentication authentication) {
        if (!currentUserService.isTeacher(authentication)) {
            return null;
        }
        return currentUserService.getCurrentTeacherId(authentication);
    }

    public void assertTeacherOwnsArrangement(Authentication authentication, Long arrangementId) {
        if (arrangementId == null || !currentUserService.isTeacher(authentication)) {
            return;
        }
        CourseArrangement arrangement = courseArrangementMapper.selectById(arrangementId);
        if (arrangement == null) {
            throw new BusinessException(404, "Course arrangement not found");
        }
        Long teacherId = currentUserService.getCurrentTeacherId(authentication);
        if (!teacherId.equals(arrangement.getTeacherId())) {
            throw new BusinessException(403, "Forbidden");
        }
    }

    public void assertTeacherOwnsArrangements(Authentication authentication, Collection<Long> arrangementIds) {
        if (arrangementIds == null || arrangementIds.isEmpty()) {
            return;
        }
        for (Long arrangementId : arrangementIds) {
            assertTeacherOwnsArrangement(authentication, arrangementId);
        }
    }

    public boolean isStudent(Authentication authentication) {
        return currentUserService.getCurrentUser(authentication).getRole() == SysUser.Role.STUDENT;
    }
}
