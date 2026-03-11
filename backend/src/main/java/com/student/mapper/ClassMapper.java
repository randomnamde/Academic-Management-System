package com.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.entity.Class;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.Year;
import java.util.List;

@Mapper
public interface ClassMapper extends BaseMapper<Class> {
    
    @Select("""
            SELECT c.*, t.name AS teacher_name, m.major_name
            FROM class c
            LEFT JOIN teacher t ON c.teacher_id = t.id
            LEFT JOIN major m ON m.major_code = c.major_code
            WHERE c.id = #{id}
            """)
    Class selectByIdWithTeacher(@Param("id") Long id);

    @Select("""
            SELECT c.*, t.name AS teacher_name, m.major_name
            FROM class c
            LEFT JOIN teacher t ON c.teacher_id = t.id
            LEFT JOIN major m ON m.major_code = c.major_code
            WHERE c.class_code = #{classCode}
            """)
    Class selectByCodeWithTeacher(@Param("classCode") String classCode);
    
    Page<Class> selectPageList(Page<Class> page,
                              @Param("classCode") String classCode,
                              @Param("className") String className,
                              @Param("majorCode") String majorCode,
                              @Param("grade") Integer grade);
    
    @Select("""
            SELECT c.*, m.major_name
            FROM class c
            LEFT JOIN major m ON m.major_code = c.major_code
            WHERE c.class_code = #{classCode}
            """)
    Class selectByClassCode(@Param("classCode") String classCode);
    
    @Update("UPDATE class SET student_count = student_count + #{delta} WHERE id = #{id}")
    int updateStudentCount(@Param("id") Long id, @Param("delta") int delta);
    
    @Select("""
            SELECT c.*, m.major_name
            FROM class c
            LEFT JOIN major m ON m.major_code = c.major_code
            WHERE c.teacher_id = #{teacherId}
            ORDER BY c.create_time DESC
            """)
    List<Class> selectByTeacherId(@Param("teacherId") Long teacherId);

    @Select("""
            <script>
            SELECT c.*, t.name AS teacher_name, m.major_name
            FROM class c
            LEFT JOIN teacher t ON c.teacher_id = t.id
            LEFT JOIN major m ON m.major_code = c.major_code
            <where>
              <if test='className != null and className != ""'>
                AND c.class_name LIKE CONCAT('%', #{className}, '%')
              </if>
              <if test='grade != null'>
                AND c.grade = #{grade}
              </if>
              <if test='teacherId != null'>
                AND c.teacher_id = #{teacherId}
              </if>
              <if test='collegeId != null'>
                AND c.college_id = #{collegeId}
              </if>
              <if test='majorCode != null and majorCode != ""'>
                AND c.major_code = #{majorCode}
              </if>
            </where>
            ORDER BY c.create_time DESC
            </script>
            """)
    Page<Class> selectPageWithMajor(Page<Class> page,
                                    @Param("className") String className,
                                    @Param("grade") Year grade,
                                    @Param("teacherId") Long teacherId,
                                    @Param("collegeId") Long collegeId,
                                    @Param("majorCode") String majorCode);
}
