package com.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    @Select("SELECT role_code FROM sys_user_role WHERE user_id = #{userId} ORDER BY id ASC")
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
}

