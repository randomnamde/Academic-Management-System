package com.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.entity.Attendance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface AttendanceMapper extends BaseMapper<Attendance> {

    Attendance selectByIdWithDetail(@Param("id") Long id);

    Page<Attendance> selectPageWithDetail(Page<Attendance> page,
                                           @Param("studentId") Long studentId,
                                           @Param("courseArrangementId") Long courseArrangementId,
                                           @Param("attendanceDate") LocalDate attendanceDate,
                                           @Param("status") Attendance.Status status);

    List<Attendance> selectByStudentIdAndDateRange(@Param("studentId") Long studentId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    Long countByStatus(@Param("studentId") Long studentId,
                       @Param("status") Attendance.Status status,
                       @Param("startDate") LocalDate startDate,
                       @Param("endDate") LocalDate endDate);
}
