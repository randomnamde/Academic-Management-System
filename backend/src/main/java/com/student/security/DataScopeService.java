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

    public String resolveScopedStudentNo(Authentication authentication, String requestedStudentNo) {
        if (currentUserService.isStudent(authentication)) {
            return currentUserService.getCurrentStudentNo(authentication);
        }
        return requestedStudentNo;
    }

    public String resolveScopedTeacherNo(Authentication authentication, String requestedTeacherNo) {
        if (currentUserService.isTeacher(authentication)) {
            return currentUserService.getCurrentTeacherNo(authentication);
        }
        return requestedTeacherNo;
    }

    public String resolveCurrentTeacherNo(Authentication authentication) {
        if (!currentUserService.isTeacher(authentication)) {
            return null;
        }
        return currentUserService.getCurrentTeacherNo(authentication);
    }

    public void assertTeacherOwnsArrangement(Authentication authentication, Long arrangementId) {
        if (arrangementId == null || !currentUserService.isTeacher(authentication)) {
            return;
        }
        CourseArrangement arrangement = courseArrangementMapper.selectById(arrangementId);
        if (arrangement == null) {
            throw new BusinessException(404, "Course arrangement not found");
        }
        String teacherNo = currentUserService.getCurrentTeacherNo(authentication);
        if (!teacherNo.equals(arrangement.getTeacherNo())) {
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

    public String resolveScopedCollegeCode(Authentication authentication) {
        return currentUserService.resolveManagedCollegeCode(authentication);
    }

    public void assertCollegeScope(Authentication authentication, String collegeCode) {
        String scopedCollegeCode = resolveScopedCollegeCode(authentication);
        if (scopedCollegeCode != null && collegeCode != null && !scopedCollegeCode.equals(collegeCode)) {
            throw new BusinessException(403, "Forbidden");
        }
    }

    public Set<String> resolveCollegeClassCodes(Authentication authentication) {
        String scopedCollegeCode = resolveScopedCollegeCode(authentication);
        if (scopedCollegeCode == null) {
            return Set.of();
        }
        List<Class> classes = classMapper.selectList(
                new LambdaQueryWrapper<Class>().eq(Class::getCollegeCode, scopedCollegeCode));
        Set<String> ids = new HashSet<>();
        for (Class clazz : classes) {
            if (clazz.getClassCode() != null) {
                ids.add(clazz.getClassCode());
            }
        }
        return ids;
    }

    public Set<String> resolveCollegeStudentNos(Authentication authentication) {
        Set<String> classIds = resolveCollegeClassCodes(authentication);
        if (classIds.isEmpty()) {
            return Set.of();
        }
        List<Student> students = studentMapper.selectList(
                new LambdaQueryWrapper<Student>().in(Student::getClassId, classIds));
        Set<String> studentNos = new HashSet<>();
        for (Student student : students) {
            if (student.getStudentNo() != null) {
                studentNos.add(student.getStudentNo());
            }
        }
        return studentNos;
    }

    public StudentArrangementScope resolveStudentArrangementScope(Authentication authentication) {
        Student student = currentUserService.getCurrentStudent(authentication);
        Set<String> classIds = new HashSet<>();
        Set<Long> arrangementIds = new HashSet<>();
        Set<String> teacherNos = new HashSet<>();
        Set<String> courseCodes = new HashSet<>();

        if (student.getClassId() == null) {
            return new StudentArrangementScope(student.getStudentNo(), classIds, arrangementIds, teacherNos, courseCodes);
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
            if (arrangement.getTeacherNo() != null) {
                teacherNos.add(arrangement.getTeacherNo());
            }
            if (arrangement.getCourseCode() != null) {
                courseCodes.add(arrangement.getCourseCode());
            }
            if (arrangement.getClassId() != null) {
                classIds.add(arrangement.getClassId());
            }
        }
        return new StudentArrangementScope(student.getStudentNo(), classIds, arrangementIds, teacherNos, courseCodes);
    }

    public static final class StudentArrangementScope {
        private final String studentNo;
        private final Set<String> classIds;
        private final Set<Long> arrangementIds;
        private final Set<String> teacherNos;
        private final Set<String> courseCodes;

        public StudentArrangementScope(String studentNo,
                                       Set<String> classIds,
                                       Set<Long> arrangementIds,
                                       Set<String> teacherNos,
                                       Set<String> courseCodes) {
            this.studentNo = studentNo;
            this.classIds = Collections.unmodifiableSet(new HashSet<>(classIds));
            this.arrangementIds = Collections.unmodifiableSet(new HashSet<>(arrangementIds));
            this.teacherNos = Collections.unmodifiableSet(new HashSet<>(teacherNos));
            this.courseCodes = Collections.unmodifiableSet(new HashSet<>(courseCodes));
        }

        public String getStudentNo() {
            return studentNo;
        }

        public Set<String> getClassIds() {
            return classIds;
        }

        public Set<Long> getArrangementIds() {
            return arrangementIds;
        }

        public Set<String> getTeacherNos() {
            return teacherNos;
        }

        public Set<String> getCourseCodes() {
            return courseCodes;
        }
    }
}