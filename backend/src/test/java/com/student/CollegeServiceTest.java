package com.student;

import com.student.dto.CollegeDTO;
import com.student.entity.College;
import com.student.service.CollegeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@EnableCaching
class CollegeServiceTest {

    @Autowired(required = false)
    private CollegeService collegeService;

    @Test
    void testCollegeServiceExists() {
        assertNotNull(collegeService, "CollegeService should be loaded");
    }

    @Test
    void testCollegeDTOSetters() {
        CollegeDTO dto = new CollegeDTO();
        dto.setCollegeName("测试学院");
        dto.setCollegeNameEn("Test College");
        dto.setCollegeCode("TC01");
        dto.setStatus(1);
        dto.setTuition(new BigDecimal("10000.00"));

        assertEquals("测试学院", dto.getCollegeName());
        assertEquals("Test College", dto.getCollegeNameEn());
        assertEquals("TC01", dto.getCollegeCode());
        assertEquals(1, dto.getStatus());
        assertEquals(new BigDecimal("10000.00"), dto.getTuition());
    }

    @Test
    void testCollegeEntity() {
        College college = new College();
        college.setCollegeCode("TEST001");
        college.setCollegeName("测试学院");
        college.setStatus(1);

        assertEquals("TEST001", college.getCollegeCode());
        assertEquals("测试学院", college.getCollegeName());
        assertEquals(1, college.getStatus());
    }
}
