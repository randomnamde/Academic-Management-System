package com.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.entity.StudentEmployment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface StudentEmploymentMapper extends BaseMapper<StudentEmployment> {

    List<StudentEmployment> selectPageWithDetail(@Param("offset") Integer offset, @Param("limit") Integer limit,
                                                   @Param("studentId") String studentId,
                                                   @Param("graduationYear") Integer graduationYear,
                                                   @Param("employmentStatus") String employmentStatus,
                                                   @Param("collegeCode") String collegeCode);

    Long countWithDetail(@Param("studentId") String studentId,
                          @Param("graduationYear") Integer graduationYear,
                          @Param("employmentStatus") String employmentStatus,
                          @Param("collegeCode") String collegeCode);

    List<Map<String, Object>> selectStatisticsByYear(@Param("graduationYear") Integer graduationYear,
                                                       @Param("collegeCode") String collegeCode);

    List<Map<String, Object>> selectStatisticsByIndustry(@Param("graduationYear") Integer graduationYear,
                                                          @Param("collegeCode") String collegeCode);

    List<Map<String, Object>> selectStatisticsByCompanyType(@Param("graduationYear") Integer graduationYear,
                                                              @Param("collegeCode") String collegeCode);

    Map<String, Object> selectEmploymentRate(@Param("graduationYear") Integer graduationYear,
                                               @Param("collegeCode") String collegeCode);
}
