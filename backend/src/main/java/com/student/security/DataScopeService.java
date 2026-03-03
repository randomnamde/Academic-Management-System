package com.student.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.entity.Class;
import com.student.entity.CourseArrangement;
import com.student.entity.Student;
import com.student.exception.BusinessException;
import com.student.mapper.ClassMapper;
import com.student.mapper.CourseArrangementMapper;
import com.student.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataScopeService {

    private final CurrentUserService currentUserService;
    private final CourseArrangementMapper courseArrangementMapper;
    private final StudentMapper studentMapper;
    private final ClassMapper classMapper;

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
        return currentUserService.isStudent(authentication);
    }

    public Long resolveScopedCollegeId(Authentication authentication) {
        return currentUserService.resolveManagedCollegeId(authentication);
    }

    public void assertCollegeScope(Authentication authentication, Long collegeId) {
        Long scopedCollegeId = resolveScopedCollegeId(authentication);
        if (scopedCollegeId != null && collegeId != null && !scopedCollegeId.equals(collegeId)) {
            throw new BusinessException(403, "Forbidden");
        }
    }

    public Set<Long> resolveCollegeClassIds(Authentication authentication) {
        Long scopedCollegeId = resolveScopedCollegeId(authentication);
        if (scopedCollegeId == null) {
            return Set.of();
        }
        List<Class> classes = classMapper.selectList(
                new LambdaQueryWrapper<Class>().eq(Class::getCollegeId, scopedCollegeId));
        Set<Long> ids = new HashSet<>();
        for (Class clazz : classes) {
            if (clazz.getId() != null) {
                ids.add(clazz.getId());
            }
        }
        return ids;
    }

    public Set<Long> resolveCollegeStudentIds(Authentication authentication) {
        Set<Long> classIds = resolveCollegeClassIds(authentication);
        if (classIds.isEmpty()) {
            return Set.of();
        }
        List<Student> students = studentMapper.selectList(
                new LambdaQueryWrapper<Student>().in(Student::getClassId, classIds));
        Set<Long> ids = new HashSet<>();
        for (Student student : students) {
            if (student.getId() != null) {
                ids.add(student.getId());
            }
        }
        return ids;
    }

    public StudentArrangementScope resolveStudentArrangementScope(Authentication authentication) {
        Student student = currentUserService.getCurrentStudent(authentication);
        Set<Long> classIds = new HashSet<>();
        Set<Long> arrangementIds = new HashSet<>();
        Set<Long> teacherIds = new HashSet<>();
        Set<Long> courseIds = new HashSet<>();

        if (student.getClassId() == null) {
            return new StudentArrangementScope(student.getId(), classIds, arrangementIds, teacherIds, courseIds);
        }

        classIds.add(student.getClassId());
        List<CourseArrangement> arrangements = courseArrangementMapper.selectList(
                new LambdaQueryWrapper<CourseArrangement>()
                        .eq(CourseArrangement::getClassId, student.getClassId())
                        .eq(CourseArrangement::getStatus, 1));
        for (CourseArrangement arrangement : arrangements) {
            if (arrangement.getId() != null) {
                arrangementIds.add(arrangement.getId());
            }
            if (arrangement.getTeacherId() != null) {
                teacherIds.add(arrangement.getTeacherId());
            }
            if (arrangement.getCourseId() != null) {
                courseIds.add(arrangement.getCourseId());
            }
            if (arrangement.getClassId() != null) {
                classIds.add(arrangement.getClassId());
            }
        }
        return new StudentArrangementScope(student.getId(), classIds, arrangementIds, teacherIds, courseIds);
    }

    public static final class StudentArrangementScope {
        private final Long studentId;
        private final Set<Long> classIds;
        private final Set<Long> arrangementIds;
        private final Set<Long> teacherIds;
        private final Set<Long> courseIds;

        public StudentArrangementScope(Long studentId,
                                       Set<Long> classIds,
                                       Set<Long> arrangementIds,
                                       Set<Long> teacherIds,
                                       Set<Long> courseIds) {
            this.studentId = studentId;
            this.classIds = Collections.unmodifiableSet(new HashSet<>(classIds));
            this.arrangementIds = Collections.unmodifiableSet(new HashSet<>(arrangementIds));
            this.teacherIds = Collections.unmodifiableSet(new HashSet<>(teacherIds));
            this.courseIds = Collections.unmodifiableSet(new HashSet<>(courseIds));
        }

        public Long getStudentId() {
            return studentId;
        }

        public Set<Long> getClassIds() {
            return classIds;
        }

        public Set<Long> getArrangementIds() {
            return arrangementIds;
        }

        public Set<Long> getTeacherIds() {
            return teacherIds;
        }

        public Set<Long> getCourseIds() {
            return courseIds;
        }
    }
}

