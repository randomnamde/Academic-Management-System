package com.student;

import com.student.dto.SemesterDTO;
import com.student.entity.Semester;
import com.student.service.SemesterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@EnableCaching
class SemesterServiceTest {

    @Autowired(required = false)
    private SemesterService semesterService;

    @Test
    void testSemesterServiceExists() {
        assertNotNull(semesterService, "SemesterService should be loaded");
    }

    @Test
    void testSemesterDTOSetters() {
        SemesterDTO dto = new SemesterDTO();
        dto.setSemesterCode("2024-SPRING");
        dto.setSemesterName("2024春季学期");
        dto.setStartDate(LocalDate.of(2024, 3, 1));
        dto.setEndDate(LocalDate.of(2024, 7, 15));
        dto.setRemark("测试学期");

        assertEquals("2024-SPRING", dto.getSemesterCode());
        assertEquals("2024春季学期", dto.getSemesterName());
        assertEquals(LocalDate.of(2024, 3, 1), dto.getStartDate());
        assertEquals(LocalDate.of(2024, 7, 15), dto.getEndDate());
        assertEquals("测试学期", dto.getRemark());
    }

    @Test
    void testSemesterEntity() {
        Semester semester = new Semester();
        semester.setId(1L);
        semester.setSemesterCode("2024-SPRING");
        semester.setSemesterName("2024春季学期");
        semester.setStatus(Semester.Status.PLANNED);
        semester.setStartDate(LocalDate.of(2024, 3, 1));
        semester.setEndDate(LocalDate.of(2024, 7, 15));

        assertEquals(1L, semester.getId());
        assertEquals("2024-SPRING", semester.getSemesterCode());
        assertEquals(Semester.Status.PLANNED, semester.getStatus());
    }

    @Test
    void testSemesterStatusEnum() {
        assertNotNull(Semester.Status.values());
        assertEquals(4, Semester.Status.values().length);

        assertEquals("PLANNED", Semester.Status.PLANNED.name());
        assertEquals("ACTIVE", Semester.Status.ACTIVE.name());
        assertEquals("ENDED", Semester.Status.ENDED.name());
        assertEquals("ARCHIVED", Semester.Status.ARCHIVED.name());
    }
}
