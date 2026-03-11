package com.student;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class UserImportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void schoolAdminCanImportCourseTeacherFromCsvAndGenerateAccountPattern() throws Exception {
        String token = loginAndGetToken("admin", "123456");
        String suffix = String.valueOf(System.nanoTime());
        String teacherName = "ImportTeacher" + suffix;
        String csv = """
                collegeCode,name,gender,phone,email,title,department,hireDate,status
                CS,%s,MALE,13800001111,%s@example.com,LECTURER,Computer Science,2024-09-01,1
                """.formatted(teacherName, teacherName.toLowerCase());

        MockMultipartFile file = new MockMultipartFile(
                "file", "teacher.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/user/import")
                        .file(file)
                        .param("roleType", "COURSE_TEACHER")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.successCount").value(1))
                .andExpect(jsonPath("$.data.failedCount").value(0));

        String username = jdbcTemplate.queryForObject(
                "SELECT u.username FROM sys_user u JOIN teacher t ON t.user_id = u.id WHERE t.name = ? ORDER BY u.id DESC LIMIT 1",
                String.class,
                teacherName
        );
        assertNotNull(username);
        int year = LocalDate.now().getYear();
        assertTrue(username.matches("^T00CS" + year + "\\d{4}$"));
    }

    @Test
    void teacherCannotImportUsers() throws Exception {
        String token = loginAndGetToken("teacher001", "123456");
        String csv = """
                collegeCode,name,gender
                CS,BlockedTeacher,MALE
                """;
        MockMultipartFile file = new MockMultipartFile(
                "file", "teacher.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/user/import")
                        .file(file)
                        .param("roleType", "COURSE_TEACHER")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void courseTeacherImportWithLongCollegeCodeFails() throws Exception {
        String token = loginAndGetToken("admin", "123456");
        String csv = """
                collegeCode,name,gender
                ABCDE,InvalidCollegeCode,MALE
                """;
        MockMultipartFile file = new MockMultipartFile(
                "file", "teacher.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/user/import")
                        .file(file)
                        .param("roleType", "COURSE_TEACHER")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.successCount").value(0))
                .andExpect(jsonPath("$.data.failedCount").value(1));
    }

    @Test
    void studentImportWithoutClassCodeFails() throws Exception {
        String token = loginAndGetToken("admin", "123456");
        String csv = """
                classCode,name,gender
                ,NoClassStudent,FEMALE
                """;
        MockMultipartFile file = new MockMultipartFile(
                "file", "student.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/user/import")
                        .file(file)
                        .param("roleType", "STUDENT")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.successCount").value(0))
                .andExpect(jsonPath("$.data.failedCount").value(1));
    }

    @Test
    void homeroomTeacherImportCanBindClassWhenClassCodeProvided() throws Exception {
        String token = loginAndGetToken("admin", "123456");
        String suffix = String.valueOf(System.nanoTime());
        String classCode = "HM" + suffix.substring(Math.max(0, suffix.length() - 8));
        jdbcTemplate.update(
                "INSERT INTO class (class_name, class_code, grade, major_code, college_id, teacher_id, room, student_count, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "Import Homeroom Class " + suffix,
                classCode,
                2026,
                "SOFT2301",
                1L,
                null,
                "B301",
                0,
                1
        );

        String teacherName = "Homeroom" + suffix;
        String csv = """
                collegeCode,name,gender,classCode
                CS,%s,MALE,%s
                """.formatted(teacherName, classCode);
        MockMultipartFile file = new MockMultipartFile(
                "file", "homeroom.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/user/import")
                        .file(file)
                        .param("roleType", "HOMEROOM_TEACHER")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.successCount").value(1))
                .andExpect(jsonPath("$.data.failedCount").value(0));

        Long teacherId = jdbcTemplate.queryForObject(
                "SELECT teacher_id FROM class WHERE class_code = ?",
                Long.class,
                classCode
        );
        assertNotNull(teacherId);
        assertTrue(teacherId > 0);
    }

    @Test
    void importSupportsXlsxAndPartialSuccess() throws Exception {
        String token = loginAndGetToken("admin", "123456");
        String suffix = String.valueOf(System.nanoTime());
        byte[] xlsx = buildTeacherWorkbook(
                new String[]{"collegeCode", "name", "gender"},
                new String[]{"CS", "XlsxTeacher" + suffix, "FEMALE"},
                new String[]{"ABCDE", "BadTeacher" + suffix, "MALE"}
        );

        MockMultipartFile file = new MockMultipartFile(
                "file", "teachers.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                xlsx
        );

        MvcResult result = mockMvc.perform(multipart("/user/import")
                        .file(file)
                        .param("roleType", "COURSE_TEACHER")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.successCount").value(1))
                .andExpect(jsonPath("$.data.failedCount").value(1))
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        assertTrue(root.path("data").path("totalCount").asInt() == 2);
    }

    @Test
    void courseTeacherImportSupportsChineseHeaders() throws Exception {
        String token = loginAndGetToken("admin", "123456");
        String suffix = String.valueOf(System.nanoTime());
        String teacherName = "\u4e2d\u6587\u8868\u5934\u6559\u5e08" + suffix;
        String csv = """
                \u5b66\u9662\u7f16\u7801\uff08\u5fc5\u586b\uff09,\u59d3\u540d\uff08\u5fc5\u586b\uff09,\u6027\u522b\uff08\u5fc5\u586b\uff09,\u624b\u673a\u53f7\uff08\u9009\u586b\uff09,\u90ae\u7bb1\uff08\u9009\u586b\uff09,\u804c\u79f0\uff08\u9009\u586b\uff09,\u9662\u7cfb\uff08\u9009\u586b\uff09,\u5165\u804c\u65e5\u671f\uff08\u9009\u586b\uff09,\u72b6\u6001\uff08\u9009\u586b\uff09
                CS,%s,\u7537,13800002222,%s@example.com,\u8bb2\u5e08,\u8ba1\u7b97\u673a\u5b66\u9662,2024-09-01,1
                """.formatted(teacherName, ("cn" + suffix));

        MockMultipartFile file = new MockMultipartFile(
                "file", "teacher-cn.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/user/import")
                        .file(file)
                        .param("roleType", "COURSE_TEACHER")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.successCount").value(1))
                .andExpect(jsonPath("$.data.failedCount").value(0));
    }

    @Test
    void templateDownloadUsesChineseHeaders() throws Exception {
        String token = loginAndGetToken("admin", "123456");
        MvcResult result = mockMvc.perform(get("/user/import/template")
                        .param("roleType", "STUDENT")
                        .param("fileType", "csv")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String csv = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(csv.contains("\u73ed\u7ea7\u7f16\u7801\uff08\u5fc5\u586b\uff09,\u59d3\u540d\uff08\u5fc5\u586b\uff09,\u6027\u522b\uff08\u5fc5\u586b\uff09"));
    }

    private byte[] buildTeacherWorkbook(String[] headers, String[]... rows) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("import");
            var headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            int rowIndex = 1;
            for (String[] rowValues : rows) {
                var row = sheet.createRow(rowIndex++);
                for (int i = 0; i < rowValues.length; i++) {
                    row.createCell(i).setCellValue(rowValues[i]);
                }
            }
            workbook.write(output);
            return output.toByteArray();
        }
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

