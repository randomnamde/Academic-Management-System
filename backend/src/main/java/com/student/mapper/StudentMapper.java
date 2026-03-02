package com.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {
    
    @Select("SELECT s.*, c.class_name FROM student s LEFT JOIN class c ON s.class_id = c.id WHERE s.id = #{id}")
    Student selectByIdWithClass(@Param("id") Long id);

    @Select({
            "<script>",
            "SELECT s.*, c.class_name",
            "FROM student s",
            "LEFT JOIN class c ON s.class_id = c.id",
            "<where>",
            "  <if test='studentNo != null and studentNo != \"\"'>",
            "    AND s.student_no LIKE CONCAT('%', #{studentNo}, '%')",
            "  </if>",
            "  <if test='name != null and name != \"\"'>",
            "    AND s.name LIKE CONCAT('%', #{name}, '%')",
            "  </if>",
            "  <if test='classId != null'>",
            "    AND s.class_id = #{classId}",
            "  </if>",
            "  <if test='status != null'>",
            "    AND s.status = #{status}",
            "  </if>",
            "</where>",
            "ORDER BY s.create_time DESC",
            "</script>"
    })
    Page<Student> selectPageWithClass(Page<Student> page, 
                                       @Param("studentNo") String studentNo,
                                       @Param("name") String name,
                                       @Param("classId") Long classId,
                                       @Param("status") Student.Status status);
    
    @Select("SELECT * FROM student WHERE student_no = #{studentNo}")
    Student selectByStudentNo(@Param("studentNo") String studentNo);

    @Select("SELECT student_no FROM student WHERE student_no LIKE CONCAT(#{prefix}, '%') ORDER BY student_no DESC LIMIT 1")
    String selectLatestStudentNoByPrefix(@Param("prefix") String prefix);
    
    @Select("SELECT s.*, c.class_name FROM student s LEFT JOIN class c ON s.class_id = c.id WHERE s.class_id = #{classId}")
    List<Student> selectByClassId(@Param("classId") Long classId);
    
    @Update("UPDATE student SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Student.Status status);
    
    @Select("SELECT COUNT(*) FROM student WHERE class_id = #{classId} AND status = 'ENROLLED'")
    Long countByClassId(@Param("classId") Long classId);

    @Select("SELECT gender, COUNT(*) AS count FROM student GROUP BY gender")
    List<Map<String, Object>> countByGender();
}
