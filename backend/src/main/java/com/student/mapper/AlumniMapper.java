package com.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.entity.Alumni;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AlumniMapper extends BaseMapper<Alumni> {

    List<Alumni> selectPageWithDetail(@Param("offset") Integer offset, @Param("limit") Integer limit,
                                        @Param("studentId") String studentId,
                                        @Param("name") String name,
                                        @Param("graduationYear") Integer graduationYear,
                                        @Param("collegeCode") String collegeCode,
                                        @Param("majorCode") String majorCode);

    Long countWithDetail(@Param("studentId") String studentId,
                          @Param("name") String name,
                          @Param("graduationYear") Integer graduationYear,
                          @Param("collegeCode") String collegeCode,
                          @Param("majorCode") String majorCode);
}
