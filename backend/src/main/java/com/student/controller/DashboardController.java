package com.student.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.dto.DashboardOverviewDTO;
import com.student.entity.Attendance;
import com.student.entity.Course;
import com.student.entity.CourseArrangement;
import com.student.entity.LeaveRequest;
import com.student.entity.Score;
import com.student.entity.Student;
import com.student.entity.SysUser;
import com.student.entity.Class;
import com.student.mapper.CourseArrangementMapper;
import com.student.security.CurrentUserService;
import com.student.security.DataScopeService;
import com.student.security.RoleCode;
import com.student.service.AttendanceService;
import com.student.service.ClassService;
import com.student.service.CourseService;
import com.student.service.LeaveRequestService;
import com.student.service.ScoreService;
import com.student.service.StudentService;
import com.student.service.TeacherService;
import com.student.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private static final BigDecimal LOW_SCORE_THRESHOLD = new BigDecimal("60");

    private final CurrentUserService currentUserService;
    private final DataScopeService dataScopeService;
    private final AttendanceService attendanceService;
    private final LeaveRequestService leaveRequestService;
    private final ScoreService scoreService;
    private final CourseArrangementMapper courseArrangementMapper;
    private final StudentService studentService;
    private final TeacherService teacherService;
    private final CourseService courseService;
    private final ClassService classService;

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT')")
    public ResultVO<DashboardOverviewDTO> getOverview(Authentication authentication) {
        RoleCode primaryRoleCode = currentUserService.getPrimaryRoleCode(authentication);
        if (primaryRoleCode == null) {
            return ResultVO.error(403, "Forbidden");
        }
        if (primaryRoleCode == RoleCode.COLLEGE_ADMIN) {
            return ResultVO.success(buildCollegeOverview(authentication));
        }
        SysUser.Role role = RoleCode.toUserRole(primaryRoleCode);
        if (role == null) {
            return ResultVO.error(403, "Forbidden");
        }

        Long studentId = null;
        Long teacherId = null;
        Set<Long> arrangementIds = new HashSet<>();
        Set<String> classIds = new HashSet<>();
        Set<Long> courseIds = new HashSet<>();
        Set<Long> teacherIds = new HashSet<>();

        if (role.isStudent()) {
            Student currentStudent = currentUserService.getCurrentStudent(authentication);
            studentId = currentStudent.getId();
            if (currentStudent.getClassId() != null) {
                classIds.add(currentStudent.getClassId());
                List<CourseArrangement> studentArrangements = courseArrangementMapper.selectList(
                        new LambdaQueryWrapper<CourseArrangement>()
                                .eq(CourseArrangement::getClassId, currentStudent.getClassId())
                                .eq(CourseArrangement::getStatus, 1));
                for (CourseArrangement arrangement : studentArrangements) {
                    if (arrangement.getId() != null) {
                        arrangementIds.add(arrangement.getId());
                    }
                    if (arrangement.getCourseId() != null) {
                        courseIds.add(arrangement.getCourseId());
                    }
                    if (arrangement.getTeacherId() != null) {
                        teacherIds.add(arrangement.getTeacherId());
                    }
                    if (arrangement.getClassId() != null) {
                        classIds.add(arrangement.getClassId());
                    }
                }
            }
        } else if (role.isTeacherGroup()) {
            teacherId = currentUserService.getCurrentTeacherId(authentication);
            teacherIds.add(teacherId);
            List<CourseArrangement> teacherArrangements = courseArrangementMapper.selectList(
                            new LambdaQueryWrapper<CourseArrangement>().eq(CourseArrangement::getTeacherId, teacherId))
                    .stream().toList();
            for (CourseArrangement arrangement : teacherArrangements) {
                if (arrangement.getId() != null) {
                    arrangementIds.add(arrangement.getId());
                }
                if (arrangement.getClassId() != null) {
                    classIds.add(arrangement.getClassId());
                }
                if (arrangement.getCourseId() != null) {
                    courseIds.add(arrangement.getCourseId());
                }
            }
        }

        DashboardOverviewDTO overview = new DashboardOverviewDTO();
        overview.setRole(role.name());
        fillScopeStatistics(overview, role, studentId, classIds, courseIds, teacherIds);

        List<Long> arrangementIdList = arrangementIds.isEmpty()
                ? Collections.emptyList()
                : new ArrayList<>(arrangementIds);
        overview.setPendingApprovalCount(countPending(role, studentId, teacherId));
        overview.setAbnormalTodayCount(countAbnormalByDate(LocalDate.now(), role, studentId, arrangementIdList));
        overview.setLowScoreWarningCount(countLowScore(role, studentId, arrangementIdList));
        overview.setAbnormalTrend(buildTrend(role, studentId, arrangementIdList));
        return ResultVO.success(overview);
    }

    private DashboardOverviewDTO buildCollegeOverview(Authentication authentication) {
        Set<String> classIds = dataScopeService.resolveCollegeClassCodes(authentication);
        Set<Long> arrangementIds = new HashSet<>();
        Set<Long> courseIds = new HashSet<>();
        Set<Long> teacherIds = new HashSet<>();

        if (!classIds.isEmpty()) {
            List<Class> classes = classService.lambdaQuery().in(Class::getClassCode, classIds).list();
            for (Class clazz : classes) {
                if (clazz.getTeacherId() != null) {
                    teacherIds.add(clazz.getTeacherId());
                }
            }
            List<CourseArrangement> arrangements = courseArrangementMapper.selectList(
                    new LambdaQueryWrapper<CourseArrangement>().in(CourseArrangement::getClassId, classIds));
            for (CourseArrangement arrangement : arrangements) {
                if (arrangement.getId() != null) {
                    arrangementIds.add(arrangement.getId());
                }
                if (arrangement.getCourseId() != null) {
                    courseIds.add(arrangement.getCourseId());
                }
                if (arrangement.getTeacherId() != null) {
                    teacherIds.add(arrangement.getTeacherId());
                }
            }
        }

        DashboardOverviewDTO overview = new DashboardOverviewDTO();
        overview.setRole("COLLEGE_ADMIN");
        overview.setStudentCount(classIds.isEmpty() ? 0L : studentService.lambdaQuery().in(Student::getClassId, classIds).count());
        overview.setTeacherCount((long) teacherIds.size());
        overview.setCourseCount((long) courseIds.size());
        overview.setClassCount((long) classIds.size());

        DashboardOverviewDTO.GenderStatistics genderStatistics = new DashboardOverviewDTO.GenderStatistics();
        if (!classIds.isEmpty()) {
            genderStatistics.setMale(studentService.lambdaQuery()
                    .in(Student::getClassId, classIds)
                    .eq(Student::getGender, Student.Gender.MALE)
                    .count());
            genderStatistics.setFemale(studentService.lambdaQuery()
                    .in(Student::getClassId, classIds)
                    .eq(Student::getGender, Student.Gender.FEMALE)
                    .count());
        } else {
            genderStatistics.setMale(0L);
            genderStatistics.setFemale(0L);
        }
        overview.setGenderStatistics(genderStatistics);
        overview.setCourseCategoryStatistics(buildCourseCategoryStatistics(courseIds));

        Long collegeId = currentUserService.resolveManagedCollegeId(authentication);
        overview.setPendingApprovalCount((long) leaveRequestService.getPendingRequestsForCollege(collegeId).size());

        List<Long> arrangementIdList = arrangementIds.isEmpty() ? List.of() : new ArrayList<>(arrangementIds);
        overview.setAbnormalTodayCount(countAbnormalByDate(LocalDate.now(), SysUser.Role.COURSE_TEACHER, null, arrangementIdList));
        overview.setLowScoreWarningCount(countLowScore(SysUser.Role.COURSE_TEACHER, null, arrangementIdList));
        overview.setAbnormalTrend(buildTrend(SysUser.Role.COURSE_TEACHER, null, arrangementIdList));
        return overview;
    }

    private void fillScopeStatistics(DashboardOverviewDTO overview,
                                     SysUser.Role role,
                                     Long studentId,
                                     Set<String> classIds,
                                     Set<Long> courseIds,
                                     Set<Long> teacherIds) {
        if (role.isAdminGroup()) {
            overview.setStudentCount(studentService.lambdaQuery().count());
            overview.setTeacherCount(teacherService.lambdaQuery().count());
            overview.setCourseCount(courseService.lambdaQuery().count());
            overview.setClassCount(classService.lambdaQuery().count());

            Map<String, Long> genderMap = studentService.getGenderStatistics();
            DashboardOverviewDTO.GenderStatistics genderStatistics = new DashboardOverviewDTO.GenderStatistics();
            genderStatistics.setMale(genderMap.getOrDefault("male", genderMap.getOrDefault("MALE", 0L)));
            genderStatistics.setFemale(genderMap.getOrDefault("female", genderMap.getOrDefault("FEMALE", 0L)));
            overview.setGenderStatistics(genderStatistics);

            var categoryStatistics = courseService.getCategoryStatistics();
            DashboardOverviewDTO.CourseCategoryStatistics courseCategoryStatistics = new DashboardOverviewDTO.CourseCategoryStatistics();
            courseCategoryStatistics.setRequired(categoryStatistics.getRequired() == null ? 0L : categoryStatistics.getRequired());
            courseCategoryStatistics.setElective(categoryStatistics.getElective() == null ? 0L : categoryStatistics.getElective());
            courseCategoryStatistics.setPractical(categoryStatistics.getPractical() == null ? 0L : categoryStatistics.getPractical());
            overview.setCourseCategoryStatistics(courseCategoryStatistics);
            return;
        }

        if (role.isStudent()) {
            overview.setStudentCount(1L);
            overview.setTeacherCount((long) teacherIds.size());
            overview.setCourseCount((long) courseIds.size());
            overview.setClassCount((long) classIds.size());

            DashboardOverviewDTO.GenderStatistics genderStatistics = new DashboardOverviewDTO.GenderStatistics();
            genderStatistics.setMale(studentService.lambdaQuery()
                    .eq(Student::getId, studentId)
                    .eq(Student::getGender, Student.Gender.MALE)
                    .count());
            genderStatistics.setFemale(studentService.lambdaQuery()
                    .eq(Student::getId, studentId)
                    .eq(Student::getGender, Student.Gender.FEMALE)
                    .count());
            overview.setGenderStatistics(genderStatistics);

            overview.setCourseCategoryStatistics(buildCourseCategoryStatistics(courseIds));
            return;
        }

        if (classIds.isEmpty()) {
            overview.setStudentCount(0L);
        } else {
            overview.setStudentCount(studentService.lambdaQuery().in(Student::getClassId, classIds).count());
        }
        overview.setTeacherCount(teacherIds.isEmpty() ? 1L : (long) teacherIds.size());
        overview.setCourseCount((long) courseIds.size());
        overview.setClassCount((long) classIds.size());

        DashboardOverviewDTO.GenderStatistics genderStatistics = new DashboardOverviewDTO.GenderStatistics();
        if (!classIds.isEmpty()) {
            genderStatistics.setMale(studentService.lambdaQuery()
                    .in(Student::getClassId, classIds)
                    .eq(Student::getGender, Student.Gender.MALE)
                    .count());
            genderStatistics.setFemale(studentService.lambdaQuery()
                    .in(Student::getClassId, classIds)
                    .eq(Student::getGender, Student.Gender.FEMALE)
                    .count());
        }
        overview.setGenderStatistics(genderStatistics);
        overview.setCourseCategoryStatistics(buildCourseCategoryStatistics(courseIds));
    }

    private DashboardOverviewDTO.CourseCategoryStatistics buildCourseCategoryStatistics(Set<Long> courseIds) {
        DashboardOverviewDTO.CourseCategoryStatistics courseCategoryStatistics = new DashboardOverviewDTO.CourseCategoryStatistics();
        if (courseIds.isEmpty()) {
            return courseCategoryStatistics;
        }
        List<Course> courses = courseService.lambdaQuery().in(Course::getId, courseIds).list();
        long required = courses.stream().filter(item -> item.getCategory() == Course.Category.REQUIRED).count();
        long elective = courses.stream().filter(item -> item.getCategory() == Course.Category.ELECTIVE).count();
        long practical = courses.stream().filter(item -> item.getCategory() == Course.Category.PRACTICAL).count();
        courseCategoryStatistics.setRequired(required);
        courseCategoryStatistics.setElective(elective);
        courseCategoryStatistics.setPractical(practical);
        return courseCategoryStatistics;
    }

    private Long countPending(SysUser.Role role, Long studentId, Long teacherId) {
        if (role.isStudent()) {
            return leaveRequestService.lambdaQuery()
                    .eq(LeaveRequest::getStudentId, studentId)
                    .eq(LeaveRequest::getStatus, LeaveRequest.Status.PENDING)
                    .count();
        }
        if (role.isTeacherGroup()) {
            return (long) leaveRequestService.getPendingRequestsForTeacher(teacherId).size();
        }
        return leaveRequestService.lambdaQuery()
                .eq(LeaveRequest::getStatus, LeaveRequest.Status.PENDING)
                .count();
    }

    private Long countAbnormalByDate(LocalDate date,
                                     SysUser.Role role,
                                     Long studentId,
                                     List<Long> teacherArrangementIds) {
        var query = attendanceService.lambdaQuery()
                .eq(Attendance::getAttendanceDate, date)
                .in(Attendance::getStatus, Attendance.Status.ABSENT, Attendance.Status.LATE);
        if (role.isStudent()) {
            query.eq(Attendance::getStudentId, studentId);
        } else if (role.isTeacherGroup()) {
            if (teacherArrangementIds.isEmpty()) {
                return 0L;
            }
            query.in(Attendance::getCourseArrangementId, teacherArrangementIds);
        }
        return query.count();
    }

    private Long countLowScore(SysUser.Role role, Long studentId, List<Long> teacherArrangementIds) {
        var query = scoreService.lambdaQuery().lt(Score::getTotalScore, LOW_SCORE_THRESHOLD);
        if (role.isStudent()) {
            query.eq(Score::getStudentId, studentId);
        } else if (role.isTeacherGroup()) {
            if (teacherArrangementIds.isEmpty()) {
                return 0L;
            }
            query.in(Score::getCourseArrangementId, teacherArrangementIds);
        }
        return query.count();
    }

    private List<DashboardOverviewDTO.TrendPoint> buildTrend(SysUser.Role role,
                                                             Long studentId,
                                                             List<Long> teacherArrangementIds) {
        List<DashboardOverviewDTO.TrendPoint> trendPoints = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Long count = countAbnormalByDate(date, role, studentId, teacherArrangementIds);
            trendPoints.add(new DashboardOverviewDTO.TrendPoint(date, count));
        }
        return trendPoints;
    }
}

