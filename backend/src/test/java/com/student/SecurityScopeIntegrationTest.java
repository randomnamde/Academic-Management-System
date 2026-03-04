package com.student;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.Set;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class SecurityScopeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void studentCannotCreateScore() throws Exception {
        String token = loginAndGetToken("student001", "123456");
        String body = """
                {
                  "studentId": 1,
                  "courseArrangementId": 1,
                  "usualScore": 80,
                  "midtermScore": 80,
                  "finalScore": 80,
                  "status": "NORMAL"
                }
                """;

        mockMvc.perform(post("/score")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void studentQueryStudentScoreIsForcedToSelfScope() throws Exception {
        String token = loginAndGetToken("student001", "123456");
        MvcResult result = mockMvc.perform(get("/score/student/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode data = root.path("data");
        assertFalse(data.isEmpty());
        for (JsonNode node : data) {
            assertEquals(1L, node.path("studentId").asLong());
        }
    }

    @Test
    void teacherCannotCreateArrangementForOtherTeacher() throws Exception {
        String token = loginAndGetToken("teacher001", "123456");
        String body = """
                {
                  "courseId": 1,
                  "teacherId": 999,
                  "classId": 1,
                  "semester": "2026-2027-1",
                  "schedule": "周三 08:00-09:40",
                  "room": "A201",
                  "capacity": 50,
                  "status": 1
                }
                """;

        mockMvc.perform(post("/course-arrangement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void studentCanGetDashboardOverview() throws Exception {
        String token = loginAndGetToken("student001", "123456");
        mockMvc.perform(get("/dashboard/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.role").value("STUDENT"))
                .andExpect(jsonPath("$.data.studentCount").exists())
                .andExpect(jsonPath("$.data.teacherCount").exists())
                .andExpect(jsonPath("$.data.courseCount").exists())
                .andExpect(jsonPath("$.data.classCount").exists())
                .andExpect(jsonPath("$.data.genderStatistics").exists())
                .andExpect(jsonPath("$.data.courseCategoryStatistics").exists())
                .andExpect(jsonPath("$.data.pendingApprovalCount").exists())
                .andExpect(jsonPath("$.data.abnormalTrend").isArray());
    }

    @Test
    void studentTeacherListIsScopedToOwnArrangements() throws Exception {
        String token = loginAndGetToken("student001", "123456");
        Set<Long> teacherIds = resolveStudentScopeIds(token, "teacherId");

        MvcResult result = mockMvc.perform(get("/teacher")
                        .param("page", "1")
                        .param("size", "100")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode records = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data")
                .path("records");
        for (JsonNode node : records) {
            assertTrue(teacherIds.contains(node.path("id").asLong()));
        }
    }

    @Test
    void studentCannotAccessTeacherOutsideOwnScope() throws Exception {
        String token = loginAndGetToken("student001", "123456");
        Set<Long> teacherIds = resolveStudentScopeIds(token, "teacherId");
        Long outOfScopeId = teacherIds.stream().findFirst().orElse(1L) + 100000L;

        mockMvc.perform(get("/teacher/" + outOfScopeId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void studentCourseListIsScopedToOwnArrangements() throws Exception {
        String token = loginAndGetToken("student001", "123456");
        Set<Long> courseIds = resolveStudentScopeIds(token, "courseId");

        MvcResult result = mockMvc.perform(get("/course")
                        .param("page", "1")
                        .param("size", "100")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode records = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data")
                .path("records");
        for (JsonNode node : records) {
            assertTrue(courseIds.contains(node.path("id").asLong()));
        }
    }

    @Test
    void studentCannotAccessCourseOutsideOwnScope() throws Exception {
        String token = loginAndGetToken("student001", "123456");
        Set<Long> courseIds = resolveStudentScopeIds(token, "courseId");
        Long outOfScopeId = courseIds.stream().findFirst().orElse(1L) + 100000L;

        mockMvc.perform(get("/course/" + outOfScopeId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void studentClassListIsScopedToOwnArrangements() throws Exception {
        String token = loginAndGetToken("student001", "123456");
        Set<Long> classIds = resolveStudentScopeIds(token, "classId");

        MvcResult result = mockMvc.perform(get("/class")
                        .param("page", "1")
                        .param("size", "100")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode records = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data")
                .path("records");
        for (JsonNode node : records) {
            assertTrue(classIds.contains(node.path("id").asLong()));
        }
    }

    @Test
    void studentCannotAccessClassOutsideOwnScope() throws Exception {
        String token = loginAndGetToken("student001", "123456");
        Set<Long> classIds = resolveStudentScopeIds(token, "classId");
        Long outOfScopeId = classIds.stream().findFirst().orElse(1L) + 100000L;

        mockMvc.perform(get("/class/" + outOfScopeId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void studentDashboardClassCountIsZeroWhenClassMissing() throws Exception {
        jdbcTemplate.update("UPDATE student SET class_id = NULL WHERE id = 1");
        try {
            String token = loginAndGetToken("student001", "123456");
            mockMvc.perform(get("/dashboard/overview")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.classCount").value(0));
        } finally {
            jdbcTemplate.update("UPDATE student SET class_id = 1 WHERE id = 1");
        }
    }

    @Test
    void teacherDashboardOverviewUsesTeachingScope() throws Exception {
        jdbcTemplate.update("INSERT INTO teacher (name, status) VALUES ('Scope Teacher', 1)");

        String teacherToken = loginAndGetToken("teacher001", "123456");
        String adminToken = loginAndGetToken("admin", "123456");

        MvcResult teacherResult = mockMvc.perform(get("/dashboard/overview")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        MvcResult adminResult = mockMvc.perform(get("/dashboard/overview")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode teacherData = objectMapper.readTree(teacherResult.getResponse().getContentAsString()).path("data");
        JsonNode adminData = objectMapper.readTree(adminResult.getResponse().getContentAsString()).path("data");

        assertEquals(1L, teacherData.path("teacherCount").asLong());
        assertTrue(adminData.path("teacherCount").asLong() >= 2L);
    }

    @Test
    void studentCanAccessAnalyticsEndpoints() throws Exception {
        String token = loginAndGetToken("student001", "123456");

        mockMvc.perform(get("/analytics/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.studentCount").exists())
                .andExpect(jsonPath("$.data.attendanceRate").exists());

        mockMvc.perform(get("/analytics/score-trend")
                        .param("granularity", "week")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void studentAnalyticsIgnoresCrossScopeFilters() throws Exception {
        String token = loginAndGetToken("student001", "123456");

        MvcResult overviewResult = mockMvc.perform(get("/analytics/overview")
                        .param("classId", "999")
                        .param("teacherId", "999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode overviewRoot = objectMapper.readTree(overviewResult.getResponse().getContentAsString());
        assertEquals(1L, overviewRoot.path("data").path("studentCount").asLong());

        MvcResult riskResult = mockMvc.perform(get("/analytics/risk-students")
                        .param("riskType", "low_score")
                        .param("classId", "999")
                        .param("teacherId", "999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode records = objectMapper.readTree(riskResult.getResponse().getContentAsString())
                .path("data")
                .path("records");
        for (JsonNode node : records) {
            assertEquals(1L, node.path("studentId").asLong());
        }
    }

    @Test
    void studentRiskLowScoreShowsOwnCourseDetails() throws Exception {
        String token = loginAndGetToken("student001", "123456");
        String suffix = String.valueOf(System.nanoTime());
        String semester = "risk-low-score-" + suffix;

        String courseCode = "RLS" + suffix.substring(Math.max(0, suffix.length() - 8));
        jdbcTemplate.update("""
                INSERT INTO course
                (course_name, course_code, credit, hours, category, description, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                "Risk Low Score Course " + suffix, courseCode, 2.0, 32, "ELECTIVE", "risk detail", 1
        );
        Long secondCourseId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM course", Long.class);
        assertTrue(secondCourseId != null && secondCourseId > 0);

        jdbcTemplate.update("""
                INSERT INTO course_arrangement
                (course_id, teacher_id, class_id, semester, schedule, room, capacity, enrolled_count, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                1L, 1L, 1L, semester, "Mon 08:00-09:40-" + suffix, "A401", 60, 2, 1
        );
        Long firstArrangementId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM course_arrangement", Long.class);
        assertTrue(firstArrangementId != null && firstArrangementId > 0);

        jdbcTemplate.update("""
                INSERT INTO course_arrangement
                (course_id, teacher_id, class_id, semester, schedule, room, capacity, enrolled_count, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                secondCourseId, 1L, 1L, semester, "Tue 10:00-11:40-" + suffix, "A402", 60, 2, 1
        );
        Long secondArrangementId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM course_arrangement", Long.class);
        assertTrue(secondArrangementId != null && secondArrangementId > 0);

        jdbcTemplate.update("""
                INSERT INTO score
                (student_id, course_arrangement_id, usual_score, midterm_score, final_score, total_score, gpa, status, create_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                1L, firstArrangementId, 55.0, 54.0, 53.0, 54.0, 0.0, "NORMAL"
        );
        jdbcTemplate.update("""
                INSERT INTO score
                (student_id, course_arrangement_id, usual_score, midterm_score, final_score, total_score, gpa, status, create_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                1L, firstArrangementId, 53.0, 52.0, 51.0, 52.0, 0.0, "NORMAL"
        );
        jdbcTemplate.update("""
                INSERT INTO score
                (student_id, course_arrangement_id, usual_score, midterm_score, final_score, total_score, gpa, status, create_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                1L, secondArrangementId, 58.0, 57.0, 56.0, 57.0, 0.0, "NORMAL"
        );
        jdbcTemplate.update("""
                INSERT INTO score
                (student_id, course_arrangement_id, usual_score, midterm_score, final_score, total_score, gpa, status, create_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                2L, secondArrangementId, 40.0, 39.0, 38.0, 39.0, 0.0, "NORMAL"
        );

        MvcResult result = mockMvc.perform(get("/analytics/risk-students")
                        .param("riskType", "low_score")
                        .param("semester", semester)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode records = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data")
                .path("records");
        assertEquals(2, records.size());
        for (JsonNode node : records) {
            assertEquals(1L, node.path("studentId").asLong());
            assertTrue(node.path("courseName").asText().trim().length() > 0);
            assertTrue(node.path("score").asDouble() > 0);
            assertTrue(node.path("score").asDouble() < 60.0);
        }
    }

    @Test
    void studentRiskAbnormalAttendanceShowsOwnAttendanceDetails() throws Exception {
        String token = loginAndGetToken("student001", "123456");
        String suffix = String.valueOf(System.nanoTime());
        String semester = "risk-attendance-" + suffix;

        jdbcTemplate.update("""
                INSERT INTO course_arrangement
                (course_id, teacher_id, class_id, semester, schedule, room, capacity, enrolled_count, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                1L, 1L, 1L, semester, "Wed 08:00-09:40-" + suffix, "A501", 60, 2, 1
        );
        Long arrangementId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM course_arrangement", Long.class);
        assertTrue(arrangementId != null && arrangementId > 0);

        jdbcTemplate.update("""
                INSERT INTO attendance
                (student_id, course_arrangement_id, attendance_date, status, check_in_time, remark)
                VALUES (?, ?, CURRENT_DATE, ?, ?, ?)
                """,
                1L, arrangementId, "ABSENT", "08:00:00", "risk-absent-" + suffix
        );
        jdbcTemplate.update("""
                INSERT INTO attendance
                (student_id, course_arrangement_id, attendance_date, status, check_in_time, remark)
                VALUES (?, ?, CURRENT_DATE, ?, ?, ?)
                """,
                1L, arrangementId, "LATE", "08:30:00", "risk-late-" + suffix
        );
        jdbcTemplate.update("""
                INSERT INTO attendance
                (student_id, course_arrangement_id, attendance_date, status, check_in_time, remark)
                VALUES (?, ?, CURRENT_DATE, ?, ?, ?)
                """,
                2L, arrangementId, "ABSENT", "08:00:00", "risk-other-" + suffix
        );

        MvcResult result = mockMvc.perform(get("/analytics/risk-students")
                        .param("riskType", "abnormal_attendance")
                        .param("semester", semester)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode records = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data")
                .path("records");
        assertEquals(2, records.size());
        for (JsonNode node : records) {
            assertEquals(1L, node.path("studentId").asLong());
            assertTrue(node.path("attendanceDate").asText().trim().length() > 0);
            assertTrue(Set.of("ABSENT", "LATE").contains(node.path("attendanceStatus").asText()));
            assertTrue(node.path("courseName").asText().trim().length() > 0);
        }
    }

    @Test
    void studentRiskApprovalOverdueShowsOnlyOwnOverdueSubmissions() throws Exception {
        String token = loginAndGetToken("student001", "123456");
        String suffix = String.valueOf(System.nanoTime());
        String semester = "risk-approval-" + suffix;

        jdbcTemplate.update("""
                INSERT INTO course_arrangement
                (course_id, teacher_id, class_id, semester, schedule, room, capacity, enrolled_count, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                1L, 1L, 1L, semester, "Thu 08:00-09:40-" + suffix, "A601", 60, 2, 1
        );
        Long arrangementId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM course_arrangement", Long.class);
        assertTrue(arrangementId != null && arrangementId > 0);

        jdbcTemplate.update("""
                INSERT INTO leave_request
                (student_id, course_arrangement_id, leave_type, start_time, end_time, reason, status, approver_id, create_time)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP, DATEADD('HOUR', 2, CURRENT_TIMESTAMP), ?, ?, ?, DATEADD('HOUR', -72, CURRENT_TIMESTAMP))
                """,
                1L, arrangementId, "SICK", "risk-overdue-self-" + suffix, "PENDING", 1L
        );
        jdbcTemplate.update("""
                INSERT INTO leave_request
                (student_id, course_arrangement_id, leave_type, start_time, end_time, reason, status, approver_id, create_time)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP, DATEADD('HOUR', 2, CURRENT_TIMESTAMP), ?, ?, ?, DATEADD('HOUR', -12, CURRENT_TIMESTAMP))
                """,
                1L, arrangementId, "SICK", "risk-not-overdue-self-" + suffix, "PENDING", 1L
        );
        jdbcTemplate.update("""
                INSERT INTO leave_request
                (student_id, course_arrangement_id, leave_type, start_time, end_time, reason, status, approver_id, create_time)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP, DATEADD('HOUR', 2, CURRENT_TIMESTAMP), ?, ?, ?, DATEADD('HOUR', -80, CURRENT_TIMESTAMP))
                """,
                2L, arrangementId, "SICK", "risk-overdue-other-" + suffix, "PENDING", 1L
        );

        MvcResult result = mockMvc.perform(get("/analytics/risk-students")
                        .param("riskType", "approval_overdue")
                        .param("semester", semester)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode records = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data")
                .path("records");
        assertEquals(1, records.size());
        JsonNode node = records.get(0);
        assertEquals(1L, node.path("studentId").asLong());
        assertTrue(node.path("leaveRequestId").asLong() > 0);
        assertTrue(node.path("submitTime").asText().trim().length() > 0);
        assertTrue(node.path("overdue").asBoolean());
        assertTrue(node.path("overdueHours").asDouble() >= 48.0);
    }

    @Test
    void studentOverviewPendingIncludesRejectedExcludesApproved() throws Exception {
        String token = loginAndGetToken("student001", "123456");
        String suffix = String.valueOf(System.nanoTime());
        String semester = "scope-pending-" + suffix;
        String schedule = "Wed 10:00-11:40-" + suffix;

        jdbcTemplate.update("""
                INSERT INTO course_arrangement
                (course_id, teacher_id, class_id, semester, schedule, room, capacity, enrolled_count, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                1L, 1L, 1L, semester, schedule, "A211", 60, 1, 1
        );
        Long arrangementId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM course_arrangement", Long.class);
        assertTrue(arrangementId != null && arrangementId > 0);

        String baseReason = "pending-scope-" + suffix;
        jdbcTemplate.update("""
                INSERT INTO leave_request
                (student_id, course_arrangement_id, leave_type, start_time, end_time, reason, status, approver_id)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP, DATEADD('HOUR', 2, CURRENT_TIMESTAMP), ?, ?, ?)
                """,
                1L, arrangementId, "SICK", baseReason + "-pending", "PENDING", 1L
        );
        jdbcTemplate.update("""
                INSERT INTO leave_request
                (student_id, course_arrangement_id, leave_type, start_time, end_time, reason, status, approver_id)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP, DATEADD('HOUR', 2, CURRENT_TIMESTAMP), ?, ?, ?)
                """,
                1L, arrangementId, "SICK", baseReason + "-rejected", "REJECTED", 1L
        );
        jdbcTemplate.update("""
                INSERT INTO leave_request
                (student_id, course_arrangement_id, leave_type, start_time, end_time, reason, status, approver_id)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP, DATEADD('HOUR', 2, CURRENT_TIMESTAMP), ?, ?, ?)
                """,
                1L, arrangementId, "SICK", baseReason + "-approved", "APPROVED", 1L
        );

        MvcResult result = mockMvc.perform(get("/analytics/overview")
                        .param("semester", semester)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        assertEquals(2L, data.path("pendingApprovalCount").asLong());
    }

    @Test
    void studentOverviewLowScoreCountsDistinctCourses() throws Exception {
        String token = loginAndGetToken("student001", "123456");
        String suffix = String.valueOf(System.nanoTime());
        String semester = "scope-lowscore-" + suffix;

        String courseCode = "SC" + suffix.substring(Math.max(0, suffix.length() - 8));
        jdbcTemplate.update("""
                INSERT INTO course
                (course_name, course_code, credit, hours, category, description, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                "Scope Course " + suffix, courseCode, 2.0, 32, "ELECTIVE", "scope test", 1
        );
        Long secondCourseId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM course", Long.class);
        assertTrue(secondCourseId != null && secondCourseId > 0);

        jdbcTemplate.update("""
                INSERT INTO course_arrangement
                (course_id, teacher_id, class_id, semester, schedule, room, capacity, enrolled_count, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                1L, 1L, 1L, semester, "Thu 08:00-09:40-" + suffix, "A301", 60, 1, 1
        );
        Long firstArrangementId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM course_arrangement", Long.class);
        assertTrue(firstArrangementId != null && firstArrangementId > 0);

        jdbcTemplate.update("""
                INSERT INTO course_arrangement
                (course_id, teacher_id, class_id, semester, schedule, room, capacity, enrolled_count, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                secondCourseId, 1L, 1L, semester, "Fri 14:00-15:40-" + suffix, "A302", 60, 1, 1
        );
        Long secondArrangementId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM course_arrangement", Long.class);
        assertTrue(secondArrangementId != null && secondArrangementId > 0);

        jdbcTemplate.update("""
                INSERT INTO score
                (student_id, course_arrangement_id, usual_score, midterm_score, final_score, total_score, gpa, status, create_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                1L, firstArrangementId, 55.0, 54.0, 53.0, 54.0, 0.0, "NORMAL"
        );
        jdbcTemplate.update("""
                INSERT INTO score
                (student_id, course_arrangement_id, usual_score, midterm_score, final_score, total_score, gpa, status, create_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                1L, firstArrangementId, 58.0, 57.0, 56.0, 57.0, 1.0, "NORMAL"
        );
        jdbcTemplate.update("""
                INSERT INTO score
                (student_id, course_arrangement_id, usual_score, midterm_score, final_score, total_score, gpa, status, create_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                1L, secondArrangementId, 50.0, 49.0, 48.0, 49.0, 0.0, "NORMAL"
        );

        MvcResult result = mockMvc.perform(get("/analytics/overview")
                        .param("semester", semester)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        assertEquals(2L, data.path("lowScoreRiskCount").asLong());
    }

    @Test
    void teacherAnalyticsClassFilterIsApplied() throws Exception {
        String teacherToken = loginAndGetToken("teacher001", "123456");

        MvcResult normalResult = mockMvc.perform(get("/analytics/overview")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        MvcResult filteredResult = mockMvc.perform(get("/analytics/overview")
                        .param("classId", "999")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode normalData = objectMapper.readTree(normalResult.getResponse().getContentAsString()).path("data");
        JsonNode filteredData = objectMapper.readTree(filteredResult.getResponse().getContentAsString()).path("data");

        assertTrue(normalData.path("studentCount").asLong() >= 1L);
        assertEquals(0L, filteredData.path("studentCount").asLong());
    }

    @Test
    void studentCannotAccessHiddenAnnouncementDetail() throws Exception {
        String studentToken = loginAndGetToken("student001", "123456");
        Long announcementId = createAdminOnlyAnnouncement();

        mockMvc.perform(get("/announcement/" + announcementId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void unauthenticatedCannotAccessAnalyticsOverview() throws Exception {
        mockMvc.perform(get("/analytics/overview"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanAccessAnalyticsEndpoints() throws Exception {
        String token = loginAndGetToken("admin", "123456");

        mockMvc.perform(get("/analytics/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/analytics/attendance-trend")
                        .param("granularity", "month")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/analytics/score-trend")
                        .param("granularity", "week")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/analytics/risk-students")
                        .param("riskType", "low_score")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    void teacherCannotUpdateScoreOutsideTeachingScope() throws Exception {
        String adminToken = loginAndGetToken("admin", "123456");
        String teacherToken = loginAndGetToken("teacher001", "123456");
        Long arrangementId = createArrangement(adminToken, 2L);
        Long scoreId = createScore(adminToken, arrangementId);

        String updateBody = """
                {
                  "studentId": 1,
                  "courseArrangementId": %d,
                  "usualScore": 86,
                  "midtermScore": 87,
                  "finalScore": 88,
                  "status": "NORMAL"
                }
                """.formatted(arrangementId);

        mockMvc.perform(put("/score/" + scoreId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + teacherToken)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void homeroomTeacherCanApproveShortLeaveWhenCourseTeacherDiffers() throws Exception {
        String adminToken = loginAndGetToken("admin", "123456");
        String studentToken = loginAndGetToken("student001", "123456");
        String teacherToken = loginAndGetToken("teacher001", "123456");
        Long arrangementId = createArrangement(adminToken, 2L);
        Long leaveId = submitLeaveRequestAndGetId(studentToken, arrangementId);

                mockMvc.perform(post("/leave-request/" + leaveId + "/approve")
                        .param("approved", "true")
                        .param("remark", "scope-test")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        String body = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("token").asText();
    }

    private Long createArrangement(String token, Long teacherId) throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        String semester = "2026-test-" + suffix;
        String schedule = "Fri 10:00-11:30-" + suffix;
        String body = """
                {
                  "courseId": 1,
                  "teacherId": %d,
                  "classId": 1,
                  "semester": "%s",
                  "schedule": "%s",
                  "room": "A301",
                  "capacity": 50,
                  "status": 1
                }
                """.formatted(teacherId, semester, schedule);

        mockMvc.perform(post("/course-arrangement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        MvcResult listResult = mockMvc.perform(get("/course-arrangement")
                        .param("page", "1")
                        .param("size", "5")
                        .param("teacherId", String.valueOf(teacherId))
                        .param("semester", semester)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode records = objectMapper.readTree(listResult.getResponse().getContentAsString())
                .path("data")
                .path("records");
        assertFalse(records.isEmpty());
        return records.get(0).path("id").asLong();
    }

    private Long createScore(String token, Long arrangementId) throws Exception {
        String body = """
                {
                  "studentId": 1,
                  "courseArrangementId": %d,
                  "usualScore": 80,
                  "midtermScore": 80,
                  "finalScore": 80,
                  "status": "NORMAL"
                }
                """.formatted(arrangementId);

        mockMvc.perform(post("/score")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        MvcResult listResult = mockMvc.perform(get("/score")
                        .param("page", "1")
                        .param("size", "5")
                        .param("courseArrangementId", String.valueOf(arrangementId))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode records = objectMapper.readTree(listResult.getResponse().getContentAsString())
                .path("data")
                .path("records");
        assertFalse(records.isEmpty());
        return records.get(0).path("id").asLong();
    }

    private Long submitLeaveRequestAndGetId(String token, Long arrangementId) throws Exception {
        String body = """
                {
                  "courseArrangementId": %d,
                  "leaveType": "SICK",
                  "startTime": "2026-03-01T08:00:00",
                  "endTime": "2026-03-01T10:00:00",
                  "reason": "scope-test"
                }
                """.formatted(arrangementId);

        mockMvc.perform(post("/leave-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        MvcResult listResult = mockMvc.perform(get("/leave-request")
                        .param("page", "1")
                        .param("size", "20")
                        .param("status", "PENDING")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode records = objectMapper.readTree(listResult.getResponse().getContentAsString())
                .path("data")
                .path("records");
        for (JsonNode node : records) {
            if (arrangementId.equals(node.path("courseArrangementId").asLong())) {
                return node.path("id").asLong();
            }
        }
        throw new AssertionError("Expected leave request not found");
    }

    private Long createAdminOnlyAnnouncement() {
        jdbcTemplate.update("""
                INSERT INTO announcement
                (title, content, type, target_role, target_class_id, priority, author_id, view_count, is_top, status, start_time, end_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, DATEADD('DAY', 1, CURRENT_TIMESTAMP))
                """,
                "Admin Internal",
                "Scope test content",
                "NOTICE",
                "ADMIN",
                null,
                1,
                1L,
                0,
                0,
                1
        );
        return jdbcTemplate.queryForObject("SELECT MAX(id) FROM announcement", Long.class);
    }

    private Set<Long> resolveStudentScopeIds(String token, String fieldName) throws Exception {
        MvcResult result = mockMvc.perform(get("/course-arrangement/options")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode records = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        Set<Long> ids = new HashSet<>();
        for (JsonNode node : records) {
            if (node.hasNonNull(fieldName)) {
                ids.add(node.path(fieldName).asLong());
            }
        }
        return ids;
    }
}
