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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class ClassIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void schoolAdminCanCreateClassWithGeneratedCode() throws Exception {
        String token = loginAndGetToken("admin", "123456");
        String suffix = String.valueOf(System.nanoTime());
        String body = """
                {
                  "className": "软件工程测试班%s",
                  "grade": "2023",
                  "majorCode": "SOFT2301",
                  "teacherId": 1,
                  "room": "T101",
                  "status": 1
                }
                """.formatted(suffix);

        mockMvc.perform(post("/class")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        MvcResult listResult = mockMvc.perform(get("/class")
                        .param("page", "1")
                        .param("size", "20")
                        .param("className", "软件工程测试班" + suffix)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode record = objectMapper.readTree(listResult.getResponse().getContentAsString())
                .path("data")
                .path("records")
                .get(0);
        String classCode = record.path("classCode").asText();
        assertTrue(classCode.matches("CSXXSOFT2023\\d{4}"));

        mockMvc.perform(get("/class/" + classCode)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.classCode").value(classCode));
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
