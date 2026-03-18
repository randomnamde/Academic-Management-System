package com.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.entity.StudentRewardPunishment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface StudentRewardPunishmentMapper extends BaseMapper<StudentRewardPunishment> {

    List<StudentRewardPunishment> selectPageWithDetail(@Param("page") Integer page, @Param("size") Integer size,
                                                        @Param("studentId") String studentId,
                                                        @Param("type") String type,
                                                        @Param("status") String status,
                                                        @Param("category") String category);

    Long countWithDetail(@Param("studentId") String studentId,
                          @Param("type") String type,
                          @Param("status") String status,
                          @Param("category") String category);
}
