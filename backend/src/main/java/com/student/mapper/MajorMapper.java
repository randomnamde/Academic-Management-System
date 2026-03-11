package com.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.entity.Major;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MajorMapper extends BaseMapper<Major> {

    @Select("""
            <script>
            SELECT m.*, c.college_name
            FROM major m
            LEFT JOIN college c ON c.id = m.college_id
            <where>
              <if test='keyword != null and keyword != ""'>
                AND (
                  m.major_code LIKE CONCAT('%', #{keyword}, '%')
                  OR m.major_name LIKE CONCAT('%', #{keyword}, '%')
                  OR m.major_abbreviation LIKE CONCAT('%', #{keyword}, '%')
                )
              </if>
              <if test='collegeId != null'>
                AND m.college_id = #{collegeId}
              </if>
              <if test='status != null'>
                AND m.status = #{status}
              </if>
            </where>
            ORDER BY m.create_time DESC
            </script>
            """)
    Page<Major> selectPageWithCollege(Page<Major> page,
                                      @Param("keyword") String keyword,
                                      @Param("collegeId") Long collegeId,
                                      @Param("status") Integer status);

    @Select("""
            <script>
            SELECT m.*, c.college_name
            FROM major m
            LEFT JOIN college c ON c.id = m.college_id
            <where>
              <if test='collegeId != null'>
                AND m.college_id = #{collegeId}
              </if>
              <if test='status != null'>
                AND m.status = #{status}
              </if>
            </where>
            ORDER BY m.major_name ASC
            </script>
            """)
    List<Major> selectOptions(@Param("collegeId") Long collegeId, @Param("status") Integer status);

    @Select("""
            SELECT m.*, c.college_name
            FROM major m
            LEFT JOIN college c ON c.id = m.college_id
            WHERE m.major_code = #{majorCode}
            """)
    Major selectByCodeWithCollege(@Param("majorCode") String majorCode);

    @Select("SELECT COUNT(*) FROM major WHERE college_id = #{collegeId}")
    Long countByCollegeId(@Param("collegeId") Long collegeId);
}
