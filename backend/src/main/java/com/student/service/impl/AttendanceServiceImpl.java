package com.student.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.dto.AttendanceDTO;
import com.student.entity.Attendance;
import com.student.exception.BusinessException;
import com.student.mapper.AttendanceMapper;
import com.student.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl extends ServiceImpl<AttendanceMapper, Attendance> implements AttendanceService {

    private final AttendanceMapper attendanceMapper;

    @Override
    @Transactional
    public void recordAttendance(AttendanceDTO attendanceDTO) {
        Attendance attendance = new Attendance();
        BeanUtils.copyProperties(attendanceDTO, attendance);
        attendanceMapper.insert(attendance);
    }

    @Override
    @Transactional
    public void updateAttendance(AttendanceDTO attendanceDTO) {
        if (attendanceDTO.getId() == null) {
            throw new BusinessException("Attendance id cannot be null");
        }

        Attendance existAttendance = attendanceMapper.selectById(attendanceDTO.getId());
        if (existAttendance == null) {
            throw new BusinessException("Attendance record not found");
        }

        Attendance attendance = new Attendance();
        BeanUtils.copyProperties(attendanceDTO, attendance);
        attendanceMapper.updateById(attendance);
    }

    @Override
    @Transactional
    public void deleteAttendance(Long id) {
        Attendance attendance = attendanceMapper.selectById(id);
        if (attendance == null) {
            throw new BusinessException("Attendance record not found");
        }

        attendanceMapper.deleteById(id);
    }

    @Override
    public Attendance getAttendanceById(Long id) {
        return attendanceMapper.selectByIdWithDetail(id);
    }

    @Override
    public Page<Attendance> getAttendancePage(Integer page,
                                              Integer size,
                                              Long studentId,
                                              Long courseArrangementId,
                                              LocalDate attendanceDate,
                                              Attendance.Status status) {
        Page<Attendance> pageParam = new Page<>(page, size);
        return attendanceMapper.selectPageWithDetail(pageParam, studentId, courseArrangementId, attendanceDate, status);
    }

    @Override
    public List<Attendance> getStudentAttendance(Long studentId, LocalDate startDate, LocalDate endDate) {
        return attendanceMapper.selectByStudentIdAndDateRange(studentId, startDate, endDate);
    }

    @Override
    public Map<String, Object> getAttendanceStatistics(Long studentId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> statistics = new HashMap<>();

        Long presentCount = attendanceMapper.countByStatus(studentId, Attendance.Status.PRESENT, startDate, endDate);
        Long absentCount = attendanceMapper.countByStatus(studentId, Attendance.Status.ABSENT, startDate, endDate);
        Long lateCount = attendanceMapper.countByStatus(studentId, Attendance.Status.LATE, startDate, endDate);
        Long leaveCount = attendanceMapper.countByStatus(studentId, Attendance.Status.LEAVE, startDate, endDate);

        long total = presentCount + absentCount + lateCount + leaveCount;

        statistics.put("presentCount", presentCount);
        statistics.put("absentCount", absentCount);
        statistics.put("lateCount", lateCount);
        statistics.put("leaveCount", leaveCount);
        statistics.put("totalCount", total);

        if (total > 0) {
            statistics.put("presentRate", (double) presentCount / total * 100);
            statistics.put("absentRate", (double) absentCount / total * 100);
            statistics.put("lateRate", (double) lateCount / total * 100);
            statistics.put("leaveRate", (double) leaveCount / total * 100);
        } else {
            statistics.put("presentRate", 0.0);
            statistics.put("absentRate", 0.0);
            statistics.put("lateRate", 0.0);
            statistics.put("leaveRate", 0.0);
        }

        return statistics;
    }

    @Override
    @Transactional
    public void batchRecordAttendance(List<AttendanceDTO> attendanceDTOList) {
        for (AttendanceDTO attendanceDTO : attendanceDTOList) {
            recordAttendance(attendanceDTO);
        }
    }

    @Override
    @Transactional
    public void checkIn(Long studentId, Long courseArrangementId) {
        Attendance attendance = new Attendance();
        attendance.setStudentId(studentId);
        attendance.setCourseArrangementId(courseArrangementId);
        attendance.setAttendanceDate(LocalDate.now());
        attendance.setCheckInTime(LocalTime.now());
        attendance.setStatus(Attendance.Status.PRESENT);

        LocalTime classStartTime = LocalTime.of(9, 0);
        if (attendance.getCheckInTime().isAfter(classStartTime.plusMinutes(15))) {
            attendance.setStatus(Attendance.Status.LATE);
        }

        attendanceMapper.insert(attendance);
    }

    @Override
    @Transactional
    public void checkOut(Long studentId, Long courseArrangementId) {
        Attendance attendance = lambdaQuery()
                .eq(Attendance::getStudentId, studentId)
                .eq(Attendance::getCourseArrangementId, courseArrangementId)
                .eq(Attendance::getAttendanceDate, LocalDate.now())
                .one();

        if (attendance == null) {
            throw new BusinessException("Please check in first");
        }

        attendance.setCheckOutTime(LocalTime.now());
        attendanceMapper.updateById(attendance);
    }
}
