package com.student;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class SecurityScopeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
                .andExpect(jsonPath("$.data.pendingApprovalCount").exists())
                .andExpect(jsonPath("$.data.abnormalTrend").isArray());
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
    void unauthenticatedCannotAccessAnalyticsOverview() throws Exception {
        mockMvc.perform(get("/analytics/overview"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanAccessAnalyticsEndpoints() throws Exception {
        String token = loginAndGetToken("demo_admin", "123456");

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
        String adminToken = loginAndGetToken("demo_admin", "123456");
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
    void teacherCannotApproveLeaveOutsideTeachingScope() throws Exception {
        String adminToken = loginAndGetToken("demo_admin", "123456");
        String studentToken = loginAndGetToken("student001", "123456");
        String teacherToken = loginAndGetToken("teacher001", "123456");
        Long arrangementId = createArrangement(adminToken, 2L);
        Long leaveId = submitLeaveRequestAndGetId(studentToken, arrangementId);

        mockMvc.perform(post("/leave-request/" + leaveId + "/approve")
                        .param("approved", "true")
                        .param("remark", "scope-test")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
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
}
