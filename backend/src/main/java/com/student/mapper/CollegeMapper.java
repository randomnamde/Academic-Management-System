package com.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.entity.College;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CollegeMapper extends BaseMapper<College> {

    @Select("SELECT * FROM college WHERE college_code = #{collegeCode}")
    College selectByCollegeCode(@Param("collegeCode") String collegeCode);

    @Select("SELECT * FROM college WHERE admin_user_id = #{adminUserId}")
    College selectByAdminUserId(@Param("adminUserId") Long adminUserId);
}

