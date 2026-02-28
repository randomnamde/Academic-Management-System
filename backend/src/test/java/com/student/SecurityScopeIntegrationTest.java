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
}
