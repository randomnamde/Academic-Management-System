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
    
    @Select("""
            SELECT s.*, c.class_name, c.college_id, co.college_name, c.major_code, m.major_name
            FROM student s
            LEFT JOIN class c ON s.class_id = c.class_code
            LEFT JOIN college co ON c.college_id = co.id
            LEFT JOIN major m ON m.major_code = c.major_code
            WHERE s.id = #{id}
            """)
    Student selectByIdWithClass(@Param("id") Long id);

    @Select({
            "<script>",
            "SELECT s.*, c.class_name, c.college_id, co.college_name, c.major_code, m.major_name",
            "FROM student s",
            "LEFT JOIN class c ON s.class_id = c.class_code",
            "LEFT JOIN college co ON c.college_id = co.id",
            "LEFT JOIN major m ON m.major_code = c.major_code",
            "WHERE s.id IN",
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>",
            "  #{id}",
            "</foreach>",
            "</script>"
    })
    List<Student> selectByIdsWithClass(@Param("ids") List<Long> ids);

    @Select({
            "<script>",
            "SELECT s.*, c.class_name, c.college_id, co.college_name, c.major_code, m.major_name",
            "FROM student s",
            "LEFT JOIN class c ON s.class_id = c.class_code",
            "LEFT JOIN college co ON c.college_id = co.id",
            "LEFT JOIN major m ON m.major_code = c.major_code",
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
            "  <if test='collegeId != null'>",
            "    AND c.college_id = #{collegeId}",
            "  </if>",
            "  <if test='majorCode != null and majorCode != \"\"'>",
            "    AND c.major_code = #{majorCode}",
            "  </if>",
            "  <if test='status != null'>",
            "    AND s.status = #{status}",
            "  </if>",
            "  <if test='classIds != null and classIds.size() > 0'>",
            "    AND s.class_id IN",
            "    <foreach item='classIdItem' collection='classIds' open='(' separator=',' close=')'>",
            "      #{classIdItem}",
            "    </foreach>",
            "  </if>",
            "</where>",
            "ORDER BY s.create_time DESC",
            "</script>"
    })
    Page<Student> selectPageWithClass(Page<Student> page,
                                      @Param("studentNo") String studentNo,
                                      @Param("name") String name,
                                      @Param("classId") String classId,
                                      @Param("collegeId") Long collegeId,
                                      @Param("majorCode") String majorCode,
                                      @Param("status") Student.Status status,
                                      @Param("classIds") List<String> classIds);
    
    @Select("SELECT * FROM student WHERE student_no = #{studentNo}")
    Student selectByStudentNo(@Param("studentNo") String studentNo);

    @Select("SELECT student_no FROM student WHERE student_no LIKE CONCAT(#{prefix}, '%') ORDER BY student_no DESC LIMIT 1")
    String selectLatestStudentNoByPrefix(@Param("prefix") String prefix);
    
    @Select("""
            SELECT s.*, c.class_name, c.college_id, co.college_name, c.major_code, m.major_name
            FROM student s
            LEFT JOIN class c ON s.class_id = c.class_code
            LEFT JOIN college co ON c.college_id = co.id
            LEFT JOIN major m ON m.major_code = c.major_code
            WHERE s.class_id = #{classId}
            """)
    List<Student> selectByClassId(@Param("classId") String classId);
    
    @Update("UPDATE student SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Student.Status status);
    
    @Select("SELECT COUNT(*) FROM student WHERE class_id = #{classId} AND status = 'ENROLLED'")
    Long countByClassId(@Param("classId") String classId);

    @Select("SELECT gender, COUNT(*) AS count FROM student GROUP BY gender")
    List<Map<String, Object>> countByGender();
}
