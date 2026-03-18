package com.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.entity.LeaveRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LeaveRequestMapper extends BaseMapper<LeaveRequest> {

    LeaveRequest selectByIdWithDetail(@Param("id") Long id);

    Page<LeaveRequest> selectPageWithDetail(Page<LeaveRequest> page,
                                              @Param("studentId") String studentId,
                                              @Param("teacherNo") String teacherNo,
                                              @Param("status") LeaveRequest.Status status);

    List<LeaveRequest> selectByStudentId(@Param("studentId") String studentId);

    List<LeaveRequest> selectPendingByTeacherNo(@Param("teacherNo") String teacherNo);
}


