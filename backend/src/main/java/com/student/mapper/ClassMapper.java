package com.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.entity.Class;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ClassMapper extends BaseMapper<Class> {
    
    @Select("SELECT c.*, t.name as teacher_name FROM class c LEFT JOIN teacher t ON c.teacher_id = t.id WHERE c.id = #{id}")
    Class selectByIdWithTeacher(@Param("id") Long id);
    
    Page<Class> selectPageList(Page<Class> page,
                              @Param("classCode") String classCode,
                              @Param("className") String className,
                              @Param("major") String major,
                              @Param("grade") Integer grade);
    
    @Select("SELECT * FROM class WHERE class_code = #{classCode}")
    Class selectByClassCode(@Param("classCode") String classCode);
    
    @Update("UPDATE class SET student_count = student_count + #{delta} WHERE id = #{id}")
    int updateStudentCount(@Param("id") Long id, @Param("delta") int delta);
    
    List<Class> selectByTeacherId(@Param("teacherId") Long teacherId);
}
