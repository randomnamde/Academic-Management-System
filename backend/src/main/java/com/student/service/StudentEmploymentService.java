package com.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.dto.StudentEmploymentDTO;
import com.student.entity.StudentEmployment;
import java.util.List;
import java.util.Map;

public interface StudentEmploymentService {

    Page<StudentEmployment> getEmploymentPage(Integer page, Integer size, String studentId,
                                                Integer graduationYear, String employmentStatus,
                                                String collegeCode);

    StudentEmployment createEmployment(StudentEmploymentDTO dto);

    StudentEmployment updateEmployment(Long id, StudentEmploymentDTO dto);

    void deleteEmployment(Long id);

    StudentEmployment getByStudentId(String studentId);

    List<Map<String, Object>> getStatisticsByYear(Integer graduationYear, String collegeCode);

    List<Map<String, Object>> getStatisticsByIndustry(Integer graduationYear, String collegeCode);

    List<Map<String, Object>> getStatisticsByCompanyType(Integer graduationYear, String collegeCode);

    Map<String, Object> getEmploymentRate(Integer graduationYear, String collegeCode);
}
