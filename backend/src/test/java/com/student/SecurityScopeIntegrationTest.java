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

import java.time.Year;
import java.util.HashSet;
import java.util.Set;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class SecurityScopeIntegrationTest {

    private static final String TEST_TEACHER_USERNAME = "T00CS20240001";
    private static final String TEST_STUDENT_USERNAME = "2023SO0001";
    private static final String OTHER_STUDENT_USERNAME = "2023SO0002";
    private static final String TEST_CLASS_CODE = "CSXXSOFT20230001";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void studentCannotCreateScore() throws Exception {
        String token = loginAndGetToken(TEST_STUDENT_USERNAME, "123456");
        String body = """
                {
                  "studentId": "%s",
                  "courseArrangementId": 1,
                  "usualScore": 80,
                  "midtermScore": 80,
                  "finalScore": 80,
                  "status": "NORMAL"
                }
                """.formatted(TEST_STUDENT_USERNAME);

        mockMvc.perform(post("/score")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void studentQueryStudentScoreIsForcedToSelfScope() throws Exception {
        String token = loginAndGetToken(TEST_STUDENT_USERNAME, "123456");
        MvcResult result = mockMvc.perform(get("/score/student/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode data = root.path("data");
        assertFalse(data.isEmpty());
        for (JsonNode node : data) {
            assertEquals(TEST_STUDENT_USERNAME, node.path("studentId").asText());
        }
    }

    @Test
    void teacherCannotCreateArrangementForOtherTeacher() throws Exception {
        String token = loginAndGetToken(TEST_TEACHER_USERNAME, "123456");
        String body = """
                {
                  "collegeId": 1,
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
    void addArrangementGeneratesArrangementCodeAndUpdateKeepsIt() throws Exception {
        String adminToken = loginAndGetToken("admin", "123456");
        String suffix = String.valueOf(System.nanoTime());
        String shortSuffix = suffix.substring(Math.max(0, suffix.length() - 6));
        String semester = "ARR" + shortSuffix;
        String schedule = "Fri 08:00-" + shortSuffix;
        String createBody = """
                {
                  "collegeId": 1,
                  "courseId": 1,
                  "teacherId": 1,
                  "classId": 1,
                  "semester": "%s",
                  "schedule": "%s",
                  "room": "A601",
                  "capacity": 55,
                  "status": 1
                }
                """.formatted(semester, schedule);

        mockMvc.perform(post("/course-arrangement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(createBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        MvcResult listResult = mockMvc.perform(get("/course-arrangement")
                        .param("page", "1")
                        .param("size", "10")
                        .param("semester", semester)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode record = objectMapper.readTree(listResult.getResponse().getContentAsString())
                .path("data")
                .path("records")
                .get(0);
        Long arrangementId = record.path("id").asLong();
        String arrangementCode = record.path("arrangementCode").asText();
        assertTrue(arrangementCode.startsWith("C" + Year.now().getValue() + "00CS0001"));
        assertTrue(arrangementCode.matches("C\\d{4}[A-Z0-9]{4}[A-Z0-9]{4}\\d{2}"));

        String updateBody = """
                {
                  "collegeId": 1,
                  "courseId": 1,
                  "teacherId": 1,
                  "classId": 1,
                  "semester": "%s",
                  "schedule": "Fri 10:00-11:40-%s",
                  "room": "A602",
                  "capacity": 58,
                  "status": 1
                }
                """.formatted(semester, suffix);

        mockMvc.perform(put("/course-arrangement/" + arrangementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/course-arrangement/" + arrangementId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.arrangementCode").value(arrangementCode))
                .andExpect(jsonPath("$.data.collegeId").value(1));
    }

    @Test
    void addArrangementRejectsTeacherOutsideCollege() throws Exception {
        String adminToken = loginAndGetToken("admin", "123456");
        String suffix = String.valueOf(System.nanoTime());

        jdbcTemplate.update("""
                INSERT INTO college (college_code, college_name, description, status)
                VALUES (?, ?, ?, ?)
                """,
                "EL" + suffix.substring(Math.max(0, suffix.length() - 6)),
                "Elsewhere College " + suffix,
                "teacher scope mismatch",
                1
        );
        Long otherCollegeId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM college", Long.class);
        jdbcTemplate.update("""
                INSERT INTO teacher (teacher_no, name, gender, title, department, college_id, hire_date, status)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_DATE, ?)
                """,
                "OT" + suffix.substring(Math.max(0, suffix.length() - 6)),
                "Other Teacher " + suffix,
                "MALE",
                "LECTURER",
                "Other",
                otherCollegeId,
                1
        );
        Long teacherId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM teacher", Long.class);

        String body = """
                {
                  "collegeId": 1,
                  "courseId": 1,
                  "teacherId": %d,
                  "classId": 1,
                  "semester": "teacher-scope-%s",
                  "schedule": "Tue 08:00-09:40-%s",
                  "room": "B201",
                  "capacity": 40,
                  "status": 1
                }
                """.formatted(teacherId, suffix, suffix);

        mockMvc.perform(post("/course-arrangement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void addArrangementRejectsClassOutsideCollege() throws Exception {
        String adminToken = loginAndGetToken("admin", "123456");
        String suffix = String.valueOf(System.nanoTime());
        String shortSuffix = suffix.substring(Math.max(0, suffix.length() - 6));

        jdbcTemplate.update("""
                INSERT INTO college (college_code, college_name, description, status)
                VALUES (?, ?, ?, ?)
                """,
                "CL" + suffix.substring(Math.max(0, suffix.length() - 6)),
                "Class Scope College " + suffix,
                "class scope mismatch",
                1
        );
        Long otherCollegeId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM college", Long.class);
        jdbcTemplate.update("""
                INSERT INTO major (major_code, major_name, major_abbreviation, college_id, description, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                "OTHR" + shortSuffix.substring(Math.max(0, shortSuffix.length() - 4)),
                "Other Major " + suffix,
                "OTHR",
                otherCollegeId,
                "scope major",
                1
        );
        String majorCode = jdbcTemplate.queryForObject("SELECT major_code FROM major WHERE college_id = ? ORDER BY major_code DESC LIMIT 1", String.class, otherCollegeId);
        jdbcTemplate.update("""
                INSERT INTO class (class_name, class_code, grade, major_code, college_id, teacher_id, room, student_count, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                "Other Class " + suffix,
                "OC" + suffix.substring(Math.max(0, suffix.length() - 6)),
                2026,
                majorCode,
                otherCollegeId,
                1L,
                "C101",
                0,
                1
        );
        Long classId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM class", Long.class);

        String body = """
                {
                  "collegeId": 1,
                  "courseId": 1,
                  "teacherId": 1,
                  "classId": %d,
                  "semester": "class-scope-%s",
                  "schedule": "Tue 10:00-11:40-%s",
                  "room": "B202",
                  "capacity": 40,
                  "status": 1
                }
                """.formatted(classId, suffix, suffix);

        mockMvc.perform(post("/course-arrangement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void collegeAdminCreateArrangementUsesManagedCollegeScope() throws Exception {
        String token = loginAndGetToken("college_admin_cs", "123456");
        String suffix = String.valueOf(System.nanoTime());
        String shortSuffix = suffix.substring(Math.max(0, suffix.length() - 6));
        String semester = "CAD" + shortSuffix;
        String body = """
                {
                  "collegeId": 999999,
                  "courseId": 1,
                  "teacherId": 1,
                  "classId": 1,
                  "semester": "%s",
                  "schedule": "Thu 08:00-%s",
                  "room": "C301",
                  "capacity": 45,
                  "status": 1
                }
                """.formatted(semester, shortSuffix);

        mockMvc.perform(post("/course-arrangement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/course-arrangement")
                        .param("page", "1")
                        .param("size", "10")
                        .param("semester", semester)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].collegeId").value(1))
                .andExpect(jsonPath("$.data.records[0].arrangementCode").value(org.hamcrest.Matchers.startsWith("C" + Year.now().getValue() + "00CS0001")));
    }

    @Test
    void arrangementCodeExhaustedReturnsClearMessage() throws Exception {
        String adminToken = loginAndGetToken("admin", "123456");
        String suffix = String.valueOf(System.nanoTime());
        String shortSuffix = suffix.substring(Math.max(0, suffix.length() - 6));
        String semester = "EXH" + shortSuffix;
        String collegeCode = ("E" + shortSuffix.substring(Math.max(0, shortSuffix.length() - 3))).toUpperCase();
        String classCode = ("K" + shortSuffix.substring(Math.max(0, shortSuffix.length() - 3))).toUpperCase();

        jdbcTemplate.update("""
                INSERT INTO college (college_code, college_name, description, status)
                VALUES (?, ?, ?, ?)
                """,
                collegeCode,
                "Exhaust College " + shortSuffix,
                "arrangement code exhaust",
                1
        );
        Long collegeId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM college", Long.class);
        jdbcTemplate.update("""
                INSERT INTO teacher (teacher_no, name, gender, title, department, college_id, hire_date, status)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_DATE, ?)
                """,
                "ET" + shortSuffix,
                "Exhaust Teacher " + shortSuffix,
                "MALE",
                "LECTURER",
                "Exhaust",
                collegeId,
                1
        );
        Long teacherId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM teacher", Long.class);
        jdbcTemplate.update("""
                    INSERT INTO major (major_code, major_name, major_abbreviation, college_id, description, status)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """,
                    "EXH" + shortSuffix.substring(Math.max(0, shortSuffix.length() - 4)),
                    "Exhaust Major " + shortSuffix,
                    "EXHA",
                    collegeId,
                    "exhaust test",
                    1
            );
        String majorCode = jdbcTemplate.queryForObject("SELECT major_code FROM major WHERE college_id = ? ORDER BY major_code DESC LIMIT 1", String.class, collegeId);
        jdbcTemplate.update("""
                INSERT INTO class (class_name, class_code, grade, major_code, college_id, teacher_id, room, student_count, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                "Exhaust Class " + shortSuffix,
                classCode,
                2026,
                majorCode,
                collegeId,
                teacherId,
                "E101",
                0,
                1
        );
        Long classId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM class", Long.class);

        String prefix = "C" + Year.now().getValue() + collegeCode + classCode;

        for (int index = 0; index < 100; index++) {
            jdbcTemplate.update("""
                    INSERT INTO course_arrangement (arrangement_code, course_id, teacher_id, class_id, semester, schedule, room, capacity, enrolled_count, status)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    prefix + String.format("%02d", index),
                    1L,
                    teacherId,
                    classCode,
                    semester,
                    "EX-" + shortSuffix + "-" + index,
                    "Z" + index,
                    60,
                    0,
                    1
            );
        }

        String body = """
                {
                  "collegeId": %d,
                  "courseId": 1,
                  "teacherId": %d,
                  "classId": %d,
                  "semester": "%s",
                  "schedule": "Fri 12:00-%s",
                  "room": "C401",
                  "capacity": 30,
                  "status": 1
                }
                """.formatted(collegeId, teacherId, classId, semester, shortSuffix);

        mockMvc.perform(post("/course-arrangement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void studentCanGetDashboardOverview() throws Exception {
        String token = loginAndGetToken(TEST_STUDENT_USERNAME, "123456");
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
        String token = loginAndGetToken(TEST_STUDENT_USERNAME, "123456");
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
        String token = loginAndGetToken(TEST_STUDENT_USERNAME, "123456");
        Set<Long> teacherIds = resolveStudentScopeIds(token, "teacherId");
        Long outOfScopeId = teacherIds.stream().findFirst().orElse(1L) + 100000L;

        mockMvc.perform(get("/teacher/" + outOfScopeId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void studentCourseListIsScopedToOwnArrangements() throws Exception {
        String token = loginAndGetToken(TEST_STUDENT_USERNAME, "123456");
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
        String token = loginAndGetToken(TEST_STUDENT_USERNAME, "123456");
        Set<Long> courseIds = resolveStudentScopeIds(token, "courseId");
        Long outOfScopeId = courseIds.stream().findFirst().orElse(1L) + 100000L;

        mockMvc.perform(get("/course/" + outOfScopeId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void studentClassListIsScopedToOwnArrangements() throws Exception {
        String token = loginAndGetToken(TEST_STUDENT_USERNAME, "123456");

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
            assertEquals(TEST_CLASS_CODE, node.path("classCode").asText());
        }
    }

    @Test
    void studentCourseArrangementListDefaultsToOwnClassCurrentSemester() throws Exception {
        String originalSemester = jdbcTemplate.queryForObject(
                "SELECT semester_code FROM semester WHERE status = 'ACTIVE' LIMIT 1",
                String.class
        );
        String suffix = String.valueOf(System.nanoTime());
        String currentSemester = "STU-" + suffix.substring(Math.max(0, suffix.length() - 6));
        String otherSemester = currentSemester + "-ALT";
        jdbcTemplate.update(
                "UPDATE semester SET semester_code = ? WHERE status = 'ACTIVE'",
                currentSemester
        );

        try {
            String studentClassCode = TEST_CLASS_CODE;
            Long studentCollegeId = jdbcTemplate.queryForObject(
                    "SELECT college_id FROM class WHERE class_code = ?",
                    Long.class,
                    studentClassCode
            );
            jdbcTemplate.update(
                    """
                    INSERT INTO class (class_name, class_code, grade, major_code, college_id, teacher_id, room, student_count, status)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    "Scope Class " + suffix,
                    "SC" + suffix.substring(Math.max(0, suffix.length() - 6)),
                    2026,
                    jdbcTemplate.queryForObject("SELECT major_code FROM class WHERE class_code = ?", String.class, studentClassCode),
                    studentCollegeId,
                    1L,
                    "S101",
                    0,
                    1
            );
            String otherClassCode = jdbcTemplate.queryForObject("SELECT class_code FROM class ORDER BY id DESC LIMIT 1", String.class);

            String adminToken = loginAndGetToken("admin", "123456");
            createArrangementForClassAndSemester(adminToken, studentClassCode, currentSemester, "Mon 08:00-" + suffix);
            createArrangementForClassAndSemester(adminToken, studentClassCode, otherSemester, "Tue 10:00-" + suffix);
            createArrangementForClassAndSemester(adminToken, otherClassCode, currentSemester, "Wed 14:00-" + suffix);

            String studentToken = loginAndGetToken(TEST_STUDENT_USERNAME, "123456");
            MvcResult result = mockMvc.perform(get("/course-arrangement")
                            .param("page", "1")
                            .param("size", "100")
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andReturn();

            JsonNode records = objectMapper.readTree(result.getResponse().getContentAsString())
                    .path("data")
                    .path("records");
            assertFalse(records.isEmpty());

            boolean foundCurrentSemesterOwnClass = false;
            for (JsonNode node : records) {
                assertEquals(studentClassCode, node.path("classId").asText());
                assertEquals(currentSemester, node.path("semester").asText());
                assertEquals(1, node.path("status").asInt());
                if (("Mon 08:00-" + suffix).equals(node.path("schedule").asText())) {
                    foundCurrentSemesterOwnClass = true;
                }
            }
            assertTrue(foundCurrentSemesterOwnClass);
        } finally {
            jdbcTemplate.update(
                    "UPDATE semester SET semester_code = ? WHERE status = 'ACTIVE'",
                    originalSemester
            );
        }
    }

    @Test
    void studentCannotAccessClassOutsideOwnScope() throws Exception {
        String token = loginAndGetToken(TEST_STUDENT_USERNAME, "123456");
        String outOfScopeClassCode = "OTHRSCPX20239999";

        mockMvc.perform(get("/class/" + outOfScopeClassCode)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void studentDashboardClassCountIsZeroWhenClassMissing() throws Exception {
        jdbcTemplate.update("UPDATE student SET class_id = NULL WHERE student_no = ?", TEST_STUDENT_USERNAME);
        try {
            String token = loginAndGetToken(TEST_STUDENT_USERNAME, "123456");
            mockMvc.perform(get("/dashboard/overview")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.classCount").value(0));
        } finally {
            jdbcTemplate.update("UPDATE student SET class_id = ? WHERE student_no = ?", TEST_CLASS_CODE, TEST_STUDENT_USERNAME);
        }
    }

    @Test
    void teacherDashboardOverviewUsesTeachingScope() throws Exception {
        jdbcTemplate.update(
                "INSERT INTO teacher (teacher_no, name, status) VALUES (?, 'Scope Teacher', 1)",
                "TSCP20260001"
        );

        String teacherToken = loginAndGetToken(TEST_TEACHER_USERNAME, "123456");
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
        String token = loginAndGetToken(TEST_STUDENT_USERNAME, "123456");

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
        String token = loginAndGetToken(TEST_STUDENT_USERNAME, "123456");

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
            assertEquals(TEST_STUDENT_USERNAME, node.path("studentId").asText());
        }
    }

    @Test
    void studentRiskLowScoreShowsOwnCourseDetails() throws Exception {
        String token = loginAndGetToken(TEST_STUDENT_USERNAME, "123456");
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
                1L, 1L, TEST_CLASS_CODE, semester, "Mon 08:00-09:40-" + suffix, "A401", 60, 2, 1
        );
        Long firstArrangementId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM course_arrangement", Long.class);
        assertTrue(firstArrangementId != null && firstArrangementId > 0);

        jdbcTemplate.update("""
                INSERT INTO course_arrangement
                (course_id, teacher_id, class_id, semester, schedule, room, capacity, enrolled_count, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                secondCourseId, 1L, TEST_CLASS_CODE, semester, "Tue 10:00-11:40-" + suffix, "A402", 60, 2, 1
        );
        Long secondArrangementId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM course_arrangement", Long.class);
        assertTrue(secondArrangementId != null && secondArrangementId > 0);

        jdbcTemplate.update("""
                INSERT INTO score
                (student_id, course_arrangement_id, usual_score, midterm_score, final_score, total_score, gpa, status, create_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                TEST_STUDENT_USERNAME, firstArrangementId, 55.0, 54.0, 53.0, 54.0, 0.0, "NORMAL"
        );
        jdbcTemplate.update("""
                INSERT INTO score
                (student_id, course_arrangement_id, usual_score, midterm_score, final_score, total_score, gpa, status, create_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                TEST_STUDENT_USERNAME, firstArrangementId, 53.0, 52.0, 51.0, 52.0, 0.0, "NORMAL"
        );
        jdbcTemplate.update("""
                INSERT INTO score
                (student_id, course_arrangement_id, usual_score, midterm_score, final_score, total_score, gpa, status, create_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                TEST_STUDENT_USERNAME, secondArrangementId, 58.0, 57.0, 56.0, 57.0, 0.0, "NORMAL"
        );
        jdbcTemplate.update("""
                INSERT INTO score
                (student_id, course_arrangement_id, usual_score, midterm_score, final_score, total_score, gpa, status, create_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                OTHER_STUDENT_USERNAME, secondArrangementId, 40.0, 39.0, 38.0, 39.0, 0.0, "NORMAL"
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
            assertEquals(TEST_STUDENT_USERNAME, node.path("studentId").asText());
            assertTrue(node.path("courseName").asText().trim().length() > 0);
            assertTrue(node.path("score").asDouble() > 0);
            assertTrue(node.path("score").asDouble() < 60.0);
        }
    }

    @Test
    void studentRiskAbnormalAttendanceShowsOwnAttendanceDetails() throws Exception {
        String token = loginAndGetToken(TEST_STUDENT_USERNAME, "123456");
        String suffix = String.valueOf(System.nanoTime());
        String semester = "risk-attendance-" + suffix;

        jdbcTemplate.update("""
                INSERT INTO course_arrangement
                (course_id, teacher_id, class_id, semester, schedule, room, capacity, enrolled_count, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                1L, 1L, TEST_CLASS_CODE, semester, "Wed 08:00-09:40-" + suffix, "A501", 60, 2, 1
        );
        Long arrangementId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM course_arrangement", Long.class);
        assertTrue(arrangementId != null && arrangementId > 0);

        jdbcTemplate.update("""
                INSERT INTO attendance
                (student_id, course_arrangement_id, attendance_date, status, check_in_time, remark)
                VALUES (?, ?, CURRENT_DATE, ?, ?, ?)
                """,
                TEST_STUDENT_USERNAME, arrangementId, "ABSENT", "08:00:00", "risk-absent-" + suffix
        );
        jdbcTemplate.update("""
                INSERT INTO attendance
                (student_id, course_arrangement_id, attendance_date, status, check_in_time, remark)
                VALUES (?, ?, CURRENT_DATE, ?, ?, ?)
                """,
                TEST_STUDENT_USERNAME, arrangementId, "LATE", "08:30:00", "risk-late-" + suffix
        );
        jdbcTemplate.update("""
                INSERT INTO attendance
                (student_id, course_arrangement_id, attendance_date, status, check_in_time, remark)
                VALUES (?, ?, CURRENT_DATE, ?, ?, ?)
                """,
                OTHER_STUDENT_USERNAME, arrangementId, "ABSENT", "08:00:00", "risk-other-" + suffix
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
            assertEquals(TEST_STUDENT_USERNAME, node.path("studentId").asText());
            assertTrue(node.path("attendanceDate").asText().trim().length() > 0);
            assertTrue(Set.of("ABSENT", "LATE").contains(node.path("attendanceStatus").asText()));
            assertTrue(node.path("courseName").asText().trim().length() > 0);
        }
    }

    @Test
    void studentRiskApprovalOverdueShowsOnlyOwnOverdueSubmissions() throws Exception {
        String token = loginAndGetToken(TEST_STUDENT_USERNAME, "123456");
        String suffix = String.valueOf(System.nanoTime());
        String semester = "risk-approval-" + suffix;

        jdbcTemplate.update("""
                INSERT INTO course_arrangement
                (course_id, teacher_id, class_id, semester, schedule, room, capacity, enrolled_count, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                1L, 1L, TEST_CLASS_CODE, semester, "Thu 08:00-09:40-" + suffix, "A601", 60, 2, 1
        );
        Long arrangementId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM course_arrangement", Long.class);
        assertTrue(arrangementId != null && arrangementId > 0);

        jdbcTemplate.update("""
                INSERT INTO leave_request
                (student_id, course_arrangement_id, leave_type, start_time, end_time, reason, status, approver_id, create_time)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP, DATEADD('HOUR', 2, CURRENT_TIMESTAMP), ?, ?, ?, DATEADD('HOUR', -72, CURRENT_TIMESTAMP))
                """,
                TEST_STUDENT_USERNAME, arrangementId, "SICK", "risk-overdue-self-" + suffix, "PENDING", 1L
        );
        jdbcTemplate.update("""
                INSERT INTO leave_request
                (student_id, course_arrangement_id, leave_type, start_time, end_time, reason, status, approver_id, create_time)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP, DATEADD('HOUR', 2, CURRENT_TIMESTAMP), ?, ?, ?, DATEADD('HOUR', -12, CURRENT_TIMESTAMP))
                """,
                TEST_STUDENT_USERNAME, arrangementId, "SICK", "risk-not-overdue-self-" + suffix, "PENDING", 1L
        );
        jdbcTemplate.update("""
                INSERT INTO leave_request
                (student_id, course_arrangement_id, leave_type, start_time, end_time, reason, status, approver_id, create_time)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP, DATEADD('HOUR', 2, CURRENT_TIMESTAMP), ?, ?, ?, DATEADD('HOUR', -80, CURRENT_TIMESTAMP))
                """,
                OTHER_STUDENT_USERNAME, arrangementId, "SICK", "risk-overdue-other-" + suffix, "PENDING", 1L
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
        assertEquals(TEST_STUDENT_USERNAME, node.path("studentId").asText());
        assertTrue(node.path("leaveRequestId").asLong() > 0);
        assertTrue(node.path("submitTime").asText().trim().length() > 0);
        assertTrue(node.path("overdue").asBoolean());
        assertTrue(node.path("overdueHours").asDouble() >= 48.0);
    }

    @Test
    void studentOverviewPendingIncludesRejectedExcludesApproved() throws Exception {
        String token = loginAndGetToken(TEST_STUDENT_USERNAME, "123456");
        String suffix = String.valueOf(System.nanoTime());
        String semester = "scope-pending-" + suffix;
        String schedule = "Wed 10:00-11:40-" + suffix;

        jdbcTemplate.update("""
                INSERT INTO course_arrangement
                (course_id, teacher_id, class_id, semester, schedule, room, capacity, enrolled_count, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                1L, 1L, TEST_CLASS_CODE, semester, schedule, "A211", 60, 1, 1
        );
        Long arrangementId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM course_arrangement", Long.class);
        assertTrue(arrangementId != null && arrangementId > 0);

        String baseReason = "pending-scope-" + suffix;
        jdbcTemplate.update("""
                INSERT INTO leave_request
                (student_id, course_arrangement_id, leave_type, start_time, end_time, reason, status, approver_id)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP, DATEADD('HOUR', 2, CURRENT_TIMESTAMP), ?, ?, ?)
                """,
                TEST_STUDENT_USERNAME, arrangementId, "SICK", baseReason + "-pending", "PENDING", 1L
        );
        jdbcTemplate.update("""
                INSERT INTO leave_request
                (student_id, course_arrangement_id, leave_type, start_time, end_time, reason, status, approver_id)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP, DATEADD('HOUR', 2, CURRENT_TIMESTAMP), ?, ?, ?)
                """,
                TEST_STUDENT_USERNAME, arrangementId, "SICK", baseReason + "-rejected", "REJECTED", 1L
        );
        jdbcTemplate.update("""
                INSERT INTO leave_request
                (student_id, course_arrangement_id, leave_type, start_time, end_time, reason, status, approver_id)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP, DATEADD('HOUR', 2, CURRENT_TIMESTAMP), ?, ?, ?)
                """,
                TEST_STUDENT_USERNAME, arrangementId, "SICK", baseReason + "-approved", "APPROVED", 1L
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
        String token = loginAndGetToken(TEST_STUDENT_USERNAME, "123456");
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
                1L, 1L, TEST_CLASS_CODE, semester, "Thu 08:00-09:40-" + suffix, "A301", 60, 1, 1
        );
        Long firstArrangementId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM course_arrangement", Long.class);
        assertTrue(firstArrangementId != null && firstArrangementId > 0);

        jdbcTemplate.update("""
                INSERT INTO course_arrangement
                (course_id, teacher_id, class_id, semester, schedule, room, capacity, enrolled_count, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                secondCourseId, 1L, TEST_CLASS_CODE, semester, "Fri 14:00-15:40-" + suffix, "A302", 60, 1, 1
        );
        Long secondArrangementId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM course_arrangement", Long.class);
        assertTrue(secondArrangementId != null && secondArrangementId > 0);

        jdbcTemplate.update("""
                INSERT INTO score
                (student_id, course_arrangement_id, usual_score, midterm_score, final_score, total_score, gpa, status, create_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                TEST_STUDENT_USERNAME, firstArrangementId, 55.0, 54.0, 53.0, 54.0, 0.0, "NORMAL"
        );
        jdbcTemplate.update("""
                INSERT INTO score
                (student_id, course_arrangement_id, usual_score, midterm_score, final_score, total_score, gpa, status, create_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                TEST_STUDENT_USERNAME, firstArrangementId, 58.0, 57.0, 56.0, 57.0, 1.0, "NORMAL"
        );
        jdbcTemplate.update("""
                INSERT INTO score
                (student_id, course_arrangement_id, usual_score, midterm_score, final_score, total_score, gpa, status, create_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                TEST_STUDENT_USERNAME, secondArrangementId, 50.0, 49.0, 48.0, 49.0, 0.0, "NORMAL"
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
        String teacherToken = loginAndGetToken(TEST_TEACHER_USERNAME, "123456");

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
        String studentToken = loginAndGetToken(TEST_STUDENT_USERNAME, "123456");
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
        String teacherToken = loginAndGetToken(TEST_TEACHER_USERNAME, "123456");
        Long arrangementId = createArrangement(adminToken, 2L);
        Long scoreId = createScore(adminToken, arrangementId);

        String updateBody = """
                {
                  "studentId": "%s",
                  "courseArrangementId": %d,
                  "usualScore": 86,
                  "midtermScore": 87,
                  "finalScore": 88,
                  "status": "NORMAL"
                }
                """.formatted(TEST_STUDENT_USERNAME, arrangementId);

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
        String studentToken = loginAndGetToken(TEST_STUDENT_USERNAME, "123456");
        String teacherToken = loginAndGetToken(TEST_TEACHER_USERNAME, "123456");
        Long arrangementId = createArrangement(adminToken, 2L);
        Long leaveId = submitLeaveRequestAndGetId(studentToken, arrangementId);

                mockMvc.perform(post("/leave-request/" + leaveId + "/approve")
                        .param("approved", "true")
                        .param("remark", "scope-test")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void schoolAdminCanBindCollegeAdminByUsername() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        String collegeCode = "TC" + suffix.substring(Math.max(0, suffix.length() - 6));
        String username = "bind_admin_" + suffix;
        jdbcTemplate.update("""
                        INSERT INTO sys_user (username, password, real_name, phone, email, status, role)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                username,
                "$2a$10$uwNOzFPaw6z3fyiwMkxuouAgn7y4UCxSY71t8se/G0HpyyTYbYE9y",
                "Bind Admin " + suffix,
                "1390000" + suffix.substring(Math.max(0, suffix.length() - 4)),
                username + "@school.com",
                1,
                "COURSE_TEACHER"
        );

        String adminToken = loginAndGetToken("admin", "123456");
        String createBody = """
                {
                  "collegeCode": "%s",
                  "collegeName": "Test College %s",
                  "description": "Bind admin by username"
                }
                """.formatted(collegeCode, suffix);

        mockMvc.perform(post("/college")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(createBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        Long collegeId = jdbcTemplate.queryForObject(
                "SELECT id FROM college WHERE college_code = ?",
                Long.class,
                collegeCode
        );

        mockMvc.perform(put("/college/" + collegeId + "/admin")
                        .param("adminUsername", username)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        String boundAdminUserId = jdbcTemplate.queryForObject(
                "SELECT admin_user_id FROM college WHERE id = ?",
                String.class,
                collegeId
        );
        String expectedUserId = username;
        assertEquals(expectedUserId, boundAdminUserId);

        MvcResult adminListResult = mockMvc.perform(get("/college")
                        .param("page", "1")
                        .param("size", "10")
                        .param("keyword", collegeCode)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode adminRecords = objectMapper.readTree(adminListResult.getResponse().getContentAsString())
                .path("data")
                .path("records");
        assertFalse(adminRecords.isEmpty());
        assertEquals(username, adminRecords.get(0).path("adminUsername").asText());

        String collegeAdminToken = loginAndGetToken(username, "123456");
        MvcResult scopedResult = mockMvc.perform(get("/college")
                        .param("page", "1")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + collegeAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode scopedRecords = objectMapper.readTree(scopedResult.getResponse().getContentAsString())
                .path("data")
                .path("records");
        assertEquals(1, scopedRecords.size());
        assertEquals(collegeId.longValue(), scopedRecords.get(0).path("id").asLong());
        assertEquals(username, scopedRecords.get(0).path("adminUsername").asText());
    }

    @Test
    void bindCollegeAdminWithUnknownUsernameReturnsClearMessage() throws Exception {
        String adminToken = loginAndGetToken("admin", "123456");
        String suffix = String.valueOf(System.nanoTime());
        String collegeCode = "NF" + suffix.substring(Math.max(0, suffix.length() - 6));

        jdbcTemplate.update("""
                INSERT INTO college (college_code, college_name, description, status)
                VALUES (?, ?, ?, ?)
                """,
                collegeCode,
                "Not Found College " + suffix,
                "bind admin error case",
                1
        );

        Long collegeId = jdbcTemplate.queryForObject(
                "SELECT id FROM college WHERE college_code = ?",
                Long.class,
                collegeCode
        );

        mockMvc.perform(put("/college/" + collegeId + "/admin")
                        .param("adminUsername", "missing_admin_" + suffix)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("管理员账号不存在"));
    }

    @Test
    void createCollegeValidationMessageUsesChinese() throws Exception {
        String adminToken = loginAndGetToken("admin", "123456");
        String body = """
                {
                  "collegeCode": "",
                  "collegeName": "Validation College"
                }
                """;

        mockMvc.perform(post("/college")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("请输入学院编码"));
    }

    @Test
    void collegeDetailNotFoundReturnsChineseMessage() throws Exception {
        String adminToken = loginAndGetToken("admin", "123456");

        mockMvc.perform(get("/college/999999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("学院不存在"));
    }

    @Test
    void userListShowsGeneratedRuleAccount() throws Exception {
        String adminToken = loginAndGetToken("admin", "123456");
        String suffix = String.valueOf(System.nanoTime());
        String account = "T00CS2026" + suffix.substring(Math.max(0, suffix.length() - 4));
        String email = account.toLowerCase() + "@school.com";
        String phone = "1370000" + suffix.substring(Math.max(0, suffix.length() - 4));

        jdbcTemplate.update("""
                        INSERT INTO sys_user (username, password, real_name, phone, email, status, role)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                account,
                "$2a$10$uwNOzFPaw6z3fyiwMkxuouAgn7y4UCxSY71t8se/G0HpyyTYbYE9y",
                "Generated Account " + suffix,
                phone,
                email,
                1,
                "COURSE_TEACHER"
        );

        jdbcTemplate.update("""
                        INSERT INTO teacher (user_id, teacher_no, name, gender, phone, email, title, department, college_id, hire_date, status)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_DATE, ?)
                        """,
                account,
                account,
                "Generated Account " + suffix,
                "MALE",
                phone,
                email,
                "LECTURER",
                "Computer Science",
                1L,
                1
        );

        mockMvc.perform(get("/user/list")
                        .param("page", "1")
                        .param("size", "10")
                        .param("account", account)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].account").value(account))
                .andExpect(jsonPath("$.data.records[0].username").value(account));
    }

    @Test
    void userInfoReturnsAccountField() throws Exception {
        String adminToken = loginAndGetToken("admin", "123456");

        mockMvc.perform(get("/user/info")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.account").value("admin"))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.createTime").isNotEmpty());
    }

    @Test
    void userInfoReturnsTeacherAndStudentSummaryFields() throws Exception {
        String teacherToken = loginAndGetToken(TEST_TEACHER_USERNAME, "123456");
        mockMvc.perform(get("/user/info")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.teacherId").isNumber())
                .andExpect(jsonPath("$.data.teacherNo").isNotEmpty())
                .andExpect(jsonPath("$.data.teacherDepartment").isNotEmpty())
                .andExpect(jsonPath("$.data.collegeName").isNotEmpty());

        String studentToken = loginAndGetToken(TEST_STUDENT_USERNAME, "123456");
        mockMvc.perform(get("/user/info")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.classId").value(TEST_CLASS_CODE))
                .andExpect(jsonPath("$.data.className").isNotEmpty())
                .andExpect(jsonPath("$.data.studentNo").isNotEmpty())
                .andExpect(jsonPath("$.data.collegeName").isNotEmpty());
    }

    @Test
    void schoolAdminCanCreateSemesterAndStudentCanReadOptions() throws Exception {
        String adminToken = loginAndGetToken("admin", "123456");
        String studentToken = loginAndGetToken(TEST_STUDENT_USERNAME, "123456");
        String suffix = String.valueOf(System.nanoTime());
        String semesterCode = "2026-2027-" + suffix.substring(Math.max(0, suffix.length() - 2));
        String body = """
                {
                  "semesterCode": "%s",
                  "startDate": "2026-09-01",
                  "endDate": "2027-01-18",
                  "remark": "integration test"
                }
                """.formatted(semesterCode);

        mockMvc.perform(post("/semester")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.semesterCode").value(semesterCode))
                .andExpect(jsonPath("$.data.status").value("PLANNED"));

        mockMvc.perform(get("/semester")
                        .param("page", "1")
                        .param("size", "10")
                        .param("semesterCode", semesterCode)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].semesterCode").value(semesterCode));

        MvcResult optionsResult = mockMvc.perform(get("/semester/options")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode options = objectMapper.readTree(optionsResult.getResponse().getContentAsString()).path("data");
        assertTrue(options.toString().contains(semesterCode));
    }

    @Test
    void createSemesterRejectsDuplicateCode() throws Exception {
        String adminToken = loginAndGetToken("admin", "123456");
        String body = """
                {
                  "semesterCode": "2024-2025-1",
                  "startDate": "2024-09-01",
                  "endDate": "2025-01-20",
                  "remark": "duplicate"
                }
                """;

        mockMvc.perform(post("/semester")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("学期编码已存在"));
    }

    @Test
    void createSemesterRejectsInvalidDateRange() throws Exception {
        String adminToken = loginAndGetToken("admin", "123456");
        String body = """
                {
                  "semesterCode": "2027-2028-1",
                  "startDate": "2027-02-01",
                  "endDate": "2027-01-01",
                  "remark": "invalid date"
                }
                """;

        mockMvc.perform(post("/semester")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("学期开始日期不能晚于结束日期"));
    }

    @Test
    void activatingSemesterUpdatesCurrentSemesterAndEndsPreviousActiveSemester() throws Exception {
        String adminToken = loginAndGetToken("admin", "123456");
        String suffix = String.valueOf(System.nanoTime());
        String semesterCode = "2027-2028-" + suffix.substring(Math.max(0, suffix.length() - 2));
        String createBody = """
                {
                  "semesterCode": "%s",
                  "startDate": "2027-09-01",
                  "endDate": "2028-01-20",
                  "remark": "activation test"
                }
                """.formatted(semesterCode);

        try {
            MvcResult createResult = mockMvc.perform(post("/semester")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + adminToken)
                            .content(createBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andReturn();

            Long semesterId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                    .path("data")
                    .path("id")
                    .asLong();

            mockMvc.perform(put("/semester/" + semesterId + "/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + adminToken)
                            .content("""
                                    {
                                      "status": "ACTIVE"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"));

            mockMvc.perform(get("/system/config/current-semester")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.currentSemester").value(semesterCode));

            String previousStatus = jdbcTemplate.queryForObject(
                    "SELECT status FROM semester WHERE semester_code = ?",
                    String.class,
                    "2024-2025-1"
            );
            assertEquals("ENDED", previousStatus);
        } finally {
            jdbcTemplate.update("UPDATE semester SET status = 'ENDED' WHERE semester_code = ?", semesterCode);
            jdbcTemplate.update("UPDATE semester SET status = 'ACTIVE' WHERE semester_code = '2024-2025-1'");
            jdbcTemplate.update("UPDATE sys_config SET config_value = '2024-2025-1' WHERE config_key = 'currentSemester'");
        }
    }

    @Test
    void legacyCurrentSemesterApiRejectsUnknownSemesterCode() throws Exception {
        String adminToken = loginAndGetToken("admin", "123456");

        mockMvc.perform(put("/system/config/current-semester")
                        .param("currentSemester", "2099-2100-9")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("学期不存在，请先在学期管理中创建"));
    }

    @Test
    void schoolAdminCanUpdateCourseTimeSlotsAndStudentCanReadThem() throws Exception {
        String adminToken = loginAndGetToken("admin", "123456");
        String studentToken = loginAndGetToken(TEST_STUDENT_USERNAME, "123456");
        String body = """
                {
                  "timeSlots": [
                    "08:10-09:50",
                    "10:10-11:50",
                    "14:10-15:50"
                  ]
                }
                """;

        try {
            mockMvc.perform(put("/system/config/course-time-slots")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + adminToken)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            mockMvc.perform(get("/system/config/course-time-slots")
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.timeSlots[0]").value("08:10-09:50"))
                    .andExpect(jsonPath("$.data.timeSlots[2]").value("14:10-15:50"));
        } finally {
            jdbcTemplate.update("""
                    MERGE INTO sys_config (config_key, config_value, description)
                    KEY(config_key)
                    VALUES (?, ?, ?)
                    """,
                    "courseTimeSlots",
                    "[\"08:00-09:40\",\"10:00-11:40\",\"14:00-15:40\",\"16:00-17:40\",\"19:00-20:40\"]",
                    "Configurable course time slots for arrangements and timetables");
        }
    }

    @Test
    void courseTimeSlotsRejectInvalidRange() throws Exception {
        String adminToken = loginAndGetToken("admin", "123456");
        String body = """
                {
                  "timeSlots": [
                    "10:00-09:40"
                  ]
                }
                """;

        mockMvc.perform(put("/system/config/course-time-slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("上课结束时间必须晚于开始时间"));
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
        String shortSuffix = suffix.substring(Math.max(0, suffix.length() - 6));
        Integer teacherCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM teacher WHERE id = ?",
                Integer.class,
                teacherId
        );
        if (teacherCount != null && teacherCount == 0) {
            jdbcTemplate.update("""
                    INSERT INTO teacher (id, teacher_no, name, gender, title, department, college_id, hire_date, status)
                    VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_DATE, ?)
                    """,
                    teacherId,
                    "TX" + shortSuffix,
                    "Scope Teacher " + shortSuffix,
                    "MALE",
                    "LECTURER",
                    "Computer Science",
                    1L,
                    1
            );
        }
        String semester = "2026-test-" + suffix;
        String schedule = "Fri 10:00-11:30-" + suffix;
        String body = """
                {
                  "collegeId": 1,
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

    private void createArrangementForClassAndSemester(String token, String classId, String semester, String schedule) throws Exception {
        String body = """
                {
                  "collegeId": 1,
                  "courseId": 1,
                  "teacherId": 1,
                  "classId": "%s",
                  "semester": "%s",
                  "schedule": "%s",
                  "room": "A401",
                  "capacity": 50,
                  "status": 1
                }
                """.formatted(classId, semester, schedule);

        mockMvc.perform(post("/course-arrangement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private Long createScore(String token, Long arrangementId) throws Exception {
        String body = """
                {
                  "studentId": "%s",
                  "courseArrangementId": %d,
                  "usualScore": 80,
                  "midtermScore": 80,
                  "finalScore": 80,
                  "status": "NORMAL"
                }
                """.formatted(TEST_STUDENT_USERNAME, arrangementId);

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
                "SCHOOL_ADMIN",
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
