package com.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.dto.AttendanceDTO;
import com.student.entity.Attendance;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AttendanceService extends IService<Attendance> {

    void recordAttendance(AttendanceDTO attendanceDTO);

    void updateAttendance(AttendanceDTO attendanceDTO);

    void deleteAttendance(Long id);

    Attendance getAttendanceById(Long id);

    Page<Attendance> getAttendancePage(Integer page, Integer size, String studentId, 
                                        String teacherNo, Long courseArrangementId, LocalDate attendanceDate,
                                        Attendance.Status status);

    List<Attendance> getStudentAttendance(String studentId, LocalDate startDate, LocalDate endDate);

    Map<String, Object> getAttendanceStatistics(String studentId, LocalDate startDate, LocalDate endDate);

    void batchRecordAttendance(List<AttendanceDTO> attendanceDTOList);

    void checkIn(String studentId, Long courseArrangementId);

    void checkOut(String studentId, Long courseArrangementId);
}


