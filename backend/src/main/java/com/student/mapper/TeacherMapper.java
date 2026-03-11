package com.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {
    
    @Select("SELECT * FROM teacher WHERE teacher_no = #{teacherNo}")
    Teacher selectByTeacherNo(@Param("teacherNo") String teacherNo);

    @Select("SELECT * FROM teacher WHERE id = #{id}")
    Teacher selectByInternalId(@Param("id") Long id);
    
    Page<Teacher> selectPageList(Page<Teacher> page,
                                @Param("teacherNo") String teacherNo,
                                @Param("name") String name,
                                @Param("department") String department,
                                @Param("title") Teacher.Title title);
    
    @Select("SELECT * FROM teacher WHERE user_id = #{userId}")
    Teacher selectByUserId(@Param("userId") String userId);
}
