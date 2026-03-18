package com.student.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface TeacherWorkloadMapper {

    Map<String, Object> selectTeachingWorkload(@Param("teacherNo") String teacherNo, @Param("semester") String semester);

    List<Map<String, Object>> selectTeachingStatistics(@Param("collegeCode") String collegeCode, @Param("semester") String semester);

    Integer countCoursesByTeacher(@Param("teacherNo") String teacherNo, @Param("semester") String semester);

    Integer sumHoursByTeacher(@Param("teacherNo") String teacherNo, @Param("semester") String semester);

    Integer countStudentsByTeacher(@Param("teacherNo") String teacherNo, @Param("semester") String semester);

    Integer countExamsByTeacher(@Param("teacherNo") String teacherNo, @Param("semester") String semester);

    Integer countInvigilateHours(@Param("teacherNo") String teacherNo, @Param("semester") String semester);

    Integer countScoreRecords(@Param("teacherNo") String teacherNo, @Param("semester") String semester);
}
