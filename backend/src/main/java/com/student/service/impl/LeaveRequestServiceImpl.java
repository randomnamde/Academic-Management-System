package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.dto.LeaveRequestDTO;
import com.student.entity.Attendance;
import com.student.entity.Class;
import com.student.entity.College;
import com.student.entity.CourseArrangement;
import com.student.entity.LeaveRequest;
import com.student.entity.LeaveRequestApproval;
import com.student.entity.LeaveRequestCc;
import com.student.entity.Student;
import com.student.entity.Teacher;
import com.student.exception.BusinessException;
import com.student.mapper.AttendanceMapper;
import com.student.mapper.ClassMapper;
import com.student.mapper.CollegeMapper;
import com.student.mapper.CourseArrangementMapper;
import com.student.mapper.LeaveRequestApprovalMapper;
import com.student.mapper.LeaveRequestCcMapper;
import com.student.mapper.LeaveRequestMapper;
import com.student.mapper.StudentMapper;
import com.student.mapper.TeacherMapper;
import com.student.security.RoleCode;
import com.student.service.LeaveRequestService;
import com.student.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LeaveRequestServiceImpl extends ServiceImpl<LeaveRequestMapper, LeaveRequest> implements LeaveRequestService {

    private final LeaveRequestMapper leaveRequestMapper;
    private final AttendanceMapper attendanceMapper;
    private final CourseArrangementMapper courseArrangementMapper;
    private final StudentMapper studentMapper;
    private final ClassMapper classMapper;
    private final TeacherMapper teacherMapper;
    private final CollegeMapper collegeMapper;
    private final LeaveRequestApprovalMapper leaveRequestApprovalMapper;
    private final LeaveRequestCcMapper leaveRequestCcMapper;
    private final SysConfigService sysConfigService;

    @Override
    @Transactional
    public void submitLeaveRequest(LeaveRequestDTO leaveRequestDTO) {
        validateRange(leaveRequestDTO.getStartTime(), leaveRequestDTO.getEndTime());
        LeaveRequest leaveRequest = new LeaveRequest();
        BeanUtils.copyProperties(leaveRequestDTO, leaveRequest);
        leaveRequest.setStatus(LeaveRequest.Status.PENDING);
        leaveRequest.setWorkflowType(resolveWorkflowType(leaveRequestDTO.getStartTime(), leaveRequestDTO.getEndTime()));
        leaveRequest.setCurrentNode(LeaveRequest.NodeCode.PENDING_HOMEROOM_REVIEW);
        leaveRequestMapper.insert(leaveRequest);
    }

    @Override
    @Transactional
    public void updateLeaveRequest(LeaveRequestDTO leaveRequestDTO) {
        if (leaveRequestDTO.getId() == null) {
            throw new BusinessException("Leave request id cannot be null");
        }
        LeaveRequest existRequest = leaveRequestMapper.selectById(leaveRequestDTO.getId());
        if (existRequest == null) {
            throw new BusinessException("Leave request not found");
        }
        if (existRequest.getStatus() != LeaveRequest.Status.PENDING
                || existRequest.getCurrentNode() != LeaveRequest.NodeCode.PENDING_HOMEROOM_REVIEW) {
            throw new BusinessException("Only requests in homeroom review can be updated");
        }
        validateRange(leaveRequestDTO.getStartTime(), leaveRequestDTO.getEndTime());
        LeaveRequest leaveRequest = new LeaveRequest();
        BeanUtils.copyProperties(leaveRequestDTO, leaveRequest);
        leaveRequest.setStatus(LeaveRequest.Status.PENDING);
        leaveRequest.setWorkflowType(resolveWorkflowType(leaveRequestDTO.getStartTime(), leaveRequestDTO.getEndTime()));
        leaveRequest.setCurrentNode(LeaveRequest.NodeCode.PENDING_HOMEROOM_REVIEW);
        leaveRequestMapper.updateById(leaveRequest);
    }

    @Override
    @Transactional
    public void cancelLeaveRequest(Long id) {
        LeaveRequest leaveRequest = leaveRequestMapper.selectById(id);
        if (leaveRequest == null) {
            throw new BusinessException("Leave request not found");
        }
        if (leaveRequest.getStatus() != LeaveRequest.Status.PENDING) {
            throw new BusinessException("Only pending requests can be cancelled");
        }
        leaveRequestMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void approveLeaveRequest(Long id,
                                    boolean approved,
                                    String remark,
                                    Long approverUserId,
                                    Long approverTeacherId,
                                    Set<RoleCode> approverRoles) {
        LeaveRequest leaveRequest = leaveRequestMapper.selectById(id);
        if (leaveRequest == null) {
            throw new BusinessException("Leave request not found");
        }
        if (leaveRequest.getStatus() != LeaveRequest.Status.PENDING) {
            throw new BusinessException("This request has already been processed");
        }

        Student student = studentMapper.selectById(leaveRequest.getStudentId());
        if (student == null) {
            throw new BusinessException("Student not found");
        }
        Class clazz = student.getClassId() == null ? null : classMapper.selectByClassCode(student.getClassId());
        College college = clazz == null || clazz.getCollegeId() == null ? null : collegeMapper.selectById(clazz.getCollegeId());
        assertApprovalPermission(leaveRequest, clazz, college, approverUserId, approverTeacherId, approverRoles);

        recordApproval(leaveRequest, approverUserId, approverTeacherId, approved, remark);

        if (!approved) {
            leaveRequest.setStatus(LeaveRequest.Status.REJECTED);
            leaveRequest.setFinalStatus(LeaveRequest.FinalStatus.REJECTED);
            leaveRequest.setCurrentNode(LeaveRequest.NodeCode.COMPLETED);
            leaveRequest.setApproveTime(LocalDateTime.now());
            leaveRequest.setApproveRemark(remark);
            leaveRequest.setApproverId(approverTeacherId);
            leaveRequestMapper.updateById(leaveRequest);
            return;
        }

        if (leaveRequest.getWorkflowType() == LeaveRequest.WorkflowType.SHORT) {
            // Short leave: homeroom approves, then (optional) notify course teacher.
            leaveRequest.setStatus(LeaveRequest.Status.APPROVED);
            leaveRequest.setFinalStatus(LeaveRequest.FinalStatus.APPROVED);
            leaveRequest.setCurrentNode(LeaveRequest.NodeCode.COMPLETED);
            leaveRequest.setApproveTime(LocalDateTime.now());
            leaveRequest.setApproveRemark(remark);
            leaveRequest.setApproverId(approverTeacherId);
            leaveRequestMapper.updateById(leaveRequest);
            syncAttendanceForApprovedLeave(leaveRequest);
            if (leaveRequest.getCourseArrangementId() != null) {
                ccCourseTeacher(leaveRequest);
            }
            return;
        }

        // Long leave requires college review after homeroom.
        if (leaveRequest.getCurrentNode() == LeaveRequest.NodeCode.PENDING_HOMEROOM_REVIEW) {
            leaveRequest.setCurrentNode(LeaveRequest.NodeCode.PENDING_COLLEGE_REVIEW);
            leaveRequest.setApproveTime(LocalDateTime.now());
            leaveRequest.setApproveRemark(remark);
            leaveRequest.setApproverId(approverTeacherId);
            leaveRequestMapper.updateById(leaveRequest);
            return;
        }

        leaveRequest.setStatus(LeaveRequest.Status.APPROVED);
        leaveRequest.setFinalStatus(LeaveRequest.FinalStatus.APPROVED);
        leaveRequest.setCurrentNode(LeaveRequest.NodeCode.COMPLETED);
        leaveRequest.setApproveTime(LocalDateTime.now());
        leaveRequest.setApproveRemark(remark);
        leaveRequest.setApproverId(approverTeacherId);
        leaveRequestMapper.updateById(leaveRequest);
        syncAttendanceForApprovedLeave(leaveRequest);
        ccLongLeaveTeachers(leaveRequest, student);
    }

    @Override
    public LeaveRequest getLeaveRequestById(Long id) {
        return leaveRequestMapper.selectByIdWithDetail(id);
    }

    @Override
    public Page<LeaveRequest> getLeaveRequestPage(Integer page, Integer size, Long studentId, Long teacherId, LeaveRequest.Status status) {
        if (teacherId == null) {
            Page<LeaveRequest> pageParam = new Page<>(page, size);
            return leaveRequestMapper.selectPageWithDetail(pageParam, studentId, null, status);
        }

        // Teacher scope includes own homeroom students and own course arrangements.
        Set<String> classIds = new HashSet<>();
        List<Class> classes = classMapper.selectList(new LambdaQueryWrapper<Class>().eq(Class::getTeacherId, teacherId));
        for (Class clazz : classes) {
            if (clazz.getClassCode() != null) {
                classIds.add(clazz.getClassCode());
            }
        }
        Set<Long> allowedStudentIds = new HashSet<>();
        if (!classIds.isEmpty()) {
            List<Student> students = studentMapper.selectList(new LambdaQueryWrapper<Student>().in(Student::getClassId, classIds));
            for (Student student : students) {
                if (student.getId() != null) {
                    allowedStudentIds.add(student.getId());
                }
            }
        }
        List<CourseArrangement> ownArrangements = courseArrangementMapper.selectList(
                new LambdaQueryWrapper<CourseArrangement>().eq(CourseArrangement::getTeacherId, teacherId));
        Set<Long> arrangementIds = new HashSet<>();
        for (CourseArrangement item : ownArrangements) {
            if (item.getId() != null) {
                arrangementIds.add(item.getId());
            }
        }
        if (allowedStudentIds.isEmpty() && arrangementIds.isEmpty()) {
            Page<LeaveRequest> empty = new Page<>(page, size, 0);
            empty.setRecords(List.of());
            return empty;
        }

        Page<LeaveRequest> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<LeaveRequest> query = new LambdaQueryWrapper<LeaveRequest>()
                .eq(studentId != null, LeaveRequest::getStudentId, studentId)
                .eq(status != null, LeaveRequest::getStatus, status)
                .and(wrapper -> {
                    if (!allowedStudentIds.isEmpty()) {
                        wrapper.in(LeaveRequest::getStudentId, allowedStudentIds);
                    }
                    if (!arrangementIds.isEmpty()) {
                        if (!allowedStudentIds.isEmpty()) {
                            wrapper.or();
                        }
                        wrapper.in(LeaveRequest::getCourseArrangementId, arrangementIds);
                    }
                })
                .orderByDesc(LeaveRequest::getCreateTime);

        Page<LeaveRequest> raw = page(pageParam, query);
        List<LeaveRequest> records = raw.getRecords().stream()
                .map(item -> leaveRequestMapper.selectByIdWithDetail(item.getId()))
                .toList();
        raw.setRecords(records);
        return raw;
    }

    @Override
    public Page<LeaveRequest> getLeaveRequestPageByStudentIds(Integer page,
                                                              Integer size,
                                                              Set<Long> studentIds,
                                                              LeaveRequest.Status status) {
        Page<LeaveRequest> pageParam = new Page<>(page, size);
        if (studentIds == null || studentIds.isEmpty()) {
            pageParam.setRecords(List.of());
            pageParam.setTotal(0);
            return pageParam;
        }
        LambdaQueryWrapper<LeaveRequest> query = new LambdaQueryWrapper<LeaveRequest>()
                .in(LeaveRequest::getStudentId, studentIds)
                .eq(status != null, LeaveRequest::getStatus, status)
                .orderByDesc(LeaveRequest::getCreateTime);
        Page<LeaveRequest> raw = page(pageParam, query);
        List<LeaveRequest> records = new ArrayList<>();
        for (LeaveRequest item : raw.getRecords()) {
            records.add(leaveRequestMapper.selectByIdWithDetail(item.getId()));
        }
        raw.setRecords(records);
        return raw;
    }

    @Override
    public List<LeaveRequest> getStudentLeaveRequests(Long studentId) {
        return leaveRequestMapper.selectByStudentId(studentId);
    }

    @Override
    public List<LeaveRequest> getPendingRequestsForTeacher(Long teacherId) {
        List<Class> classes = classMapper.selectList(new LambdaQueryWrapper<Class>().eq(Class::getTeacherId, teacherId));
        if (classes.isEmpty()) {
            return List.of();
        }
        Set<String> classIds = classes.stream().map(Class::getClassCode).collect(java.util.stream.Collectors.toSet());
        if (classIds.isEmpty()) {
            return List.of();
        }
        List<Student> students = studentMapper.selectList(new LambdaQueryWrapper<Student>().in(Student::getClassId, classIds));
        Set<Long> studentIds = students.stream().map(Student::getId).collect(java.util.stream.Collectors.toSet());
        if (studentIds.isEmpty()) {
            return List.of();
        }
        List<LeaveRequest> requests = leaveRequestMapper.selectList(
                new LambdaQueryWrapper<LeaveRequest>()
                        .eq(LeaveRequest::getStatus, LeaveRequest.Status.PENDING)
                        .eq(LeaveRequest::getCurrentNode, LeaveRequest.NodeCode.PENDING_HOMEROOM_REVIEW)
                        .in(LeaveRequest::getStudentId, studentIds)
                        .orderByAsc(LeaveRequest::getCreateTime));
        return requests.stream().map(item -> leaveRequestMapper.selectByIdWithDetail(item.getId())).toList();
    }

    @Override
    public List<LeaveRequest> getPendingRequestsForCollege(Long collegeId) {
        List<Class> classes = classMapper.selectList(new LambdaQueryWrapper<Class>().eq(Class::getCollegeId, collegeId));
        if (classes.isEmpty()) {
            return List.of();
        }
        Set<String> classIds = classes.stream().map(Class::getClassCode).collect(java.util.stream.Collectors.toSet());
        List<Student> students = studentMapper.selectList(new LambdaQueryWrapper<Student>().in(Student::getClassId, classIds));
        Set<Long> studentIds = students.stream().map(Student::getId).collect(java.util.stream.Collectors.toSet());
        if (studentIds.isEmpty()) {
            return List.of();
        }
        List<LeaveRequest> requests = leaveRequestMapper.selectList(
                new LambdaQueryWrapper<LeaveRequest>()
                        .eq(LeaveRequest::getStatus, LeaveRequest.Status.PENDING)
                        .eq(LeaveRequest::getCurrentNode, LeaveRequest.NodeCode.PENDING_COLLEGE_REVIEW)
                        .in(LeaveRequest::getStudentId, studentIds)
                        .orderByAsc(LeaveRequest::getCreateTime));
        return requests.stream().map(item -> leaveRequestMapper.selectByIdWithDetail(item.getId())).toList();
    }

    @Override
    public List<LeaveRequestCc> getCcList(Long receiverUserId) {
        return leaveRequestCcMapper.selectList(
                new LambdaQueryWrapper<LeaveRequestCc>()
                        .eq(LeaveRequestCc::getReceiverUserId, receiverUserId)
                        .orderByDesc(LeaveRequestCc::getCreateTime));
    }

    @Override
    @Transactional
    public void markCcRead(Long ccId, Long receiverUserId) {
        LeaveRequestCc cc = leaveRequestCcMapper.selectById(ccId);
        if (cc == null) {
            throw new BusinessException("CC record not found");
        }
        if (!receiverUserId.equals(cc.getReceiverUserId())) {
            throw new BusinessException(403, "Forbidden");
        }
        if (cc.getReadFlag() != null && cc.getReadFlag() == 1) {
            return;
        }
        cc.setReadFlag(1);
        cc.setReadTime(LocalDateTime.now());
        leaveRequestCcMapper.updateById(cc);
    }

    private void validateRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new BusinessException("Start and end time are required");
        }
        if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
            throw new BusinessException("Start time must be before end time");
        }
    }

    private LeaveRequest.WorkflowType resolveWorkflowType(LocalDateTime startTime, LocalDateTime endTime) {
        long minutes = Duration.between(startTime, endTime).toMinutes();
        return minutes <= 24 * 60 ? LeaveRequest.WorkflowType.SHORT : LeaveRequest.WorkflowType.LONG;
    }

    private void assertApprovalPermission(LeaveRequest leaveRequest,
                                          Class clazz,
                                          College college,
                                          Long approverUserId,
                                          Long approverTeacherId,
                                          Set<RoleCode> approverRoles) {
        if (approverRoles.contains(RoleCode.SCHOOL_ADMIN)) {
            return;
        }

        LeaveRequest.NodeCode node = leaveRequest.getCurrentNode();
        if (node == LeaveRequest.NodeCode.PENDING_HOMEROOM_REVIEW) {
            if (clazz == null || clazz.getTeacherId() == null) {
                throw new BusinessException("Homeroom teacher not configured");
            }
            if (!approverRoles.contains(RoleCode.HOMEROOM_TEACHER) || !clazz.getTeacherId().equals(approverTeacherId)) {
                throw new BusinessException(403, "Only homeroom teacher can approve this step");
            }
            return;
        }

        if (node == LeaveRequest.NodeCode.PENDING_COLLEGE_REVIEW) {
            if (college == null || college.getAdminUserId() == null) {
                throw new BusinessException("College administrator is not configured");
            }
            if (!approverRoles.contains(RoleCode.COLLEGE_ADMIN) || !college.getAdminUserId().equals(approverUserId)) {
                throw new BusinessException(403, "Only college administrator can approve this step");
            }
            return;
        }

        throw new BusinessException("Current leave request node is not approvable");
    }

    private void recordApproval(LeaveRequest leaveRequest,
                                Long approverUserId,
                                Long approverTeacherId,
                                boolean approved,
                                String remark) {
        LeaveRequestApproval approval = new LeaveRequestApproval();
        approval.setLeaveRequestId(leaveRequest.getId());
        approval.setNodeCode(leaveRequest.getCurrentNode() == null ? null : leaveRequest.getCurrentNode().name());
        approval.setApproverUserId(approverUserId);
        approval.setApproverTeacherId(approverTeacherId);
        approval.setDecision(approved ? "APPROVED" : "REJECTED");
        approval.setRemark(remark);
        leaveRequestApprovalMapper.insert(approval);
    }

    private void ccCourseTeacher(LeaveRequest leaveRequest) {
        CourseArrangement arrangement = leaveRequest.getCourseArrangementId() == null
                ? null
                : courseArrangementMapper.selectById(leaveRequest.getCourseArrangementId());
        if (arrangement == null || arrangement.getTeacherId() == null) {
            return;
        }
        Teacher teacher = teacherMapper.selectById(arrangement.getTeacherId());
        if (teacher == null || teacher.getUserId() == null) {
            return;
        }
        insertCc(leaveRequest.getId(), teacher.getUserId(), teacher.getId(), "Short leave approved; FYI");
    }

    private void ccLongLeaveTeachers(LeaveRequest leaveRequest, Student student) {
        if (leaveRequest.getCourseArrangementId() != null) {
            ccCourseTeacher(leaveRequest);
            return;
        }
        if (student.getClassId() == null) {
            return;
        }
        String semester = sysConfigService.getCurrentSemester();
        List<CourseArrangement> arrangements = courseArrangementMapper.selectList(
                new LambdaQueryWrapper<CourseArrangement>()
                        .eq(CourseArrangement::getClassId, student.getClassId())
                        .eq(CourseArrangement::getStatus, 1)
                        .eq(CourseArrangement::getSemester, semester));
        Set<Long> teacherIds = arrangements.stream()
                .map(CourseArrangement::getTeacherId)
                .filter(id -> id != null)
                .collect(java.util.stream.Collectors.toSet());
        for (Long teacherId : teacherIds) {
            Teacher teacher = teacherMapper.selectById(teacherId);
            if (teacher == null || teacher.getUserId() == null) {
                continue;
            }
            insertCc(leaveRequest.getId(), teacher.getUserId(), teacher.getId(), "Long leave approved; FYI");
        }
    }

    private void insertCc(Long leaveRequestId, Long receiverUserId, Long receiverTeacherId, String remark) {
        LeaveRequestCc exists = leaveRequestCcMapper.selectOne(
                new LambdaQueryWrapper<LeaveRequestCc>()
                        .eq(LeaveRequestCc::getLeaveRequestId, leaveRequestId)
                        .eq(LeaveRequestCc::getReceiverUserId, receiverUserId));
        if (exists != null) {
            return;
        }
        LeaveRequestCc cc = new LeaveRequestCc();
        cc.setLeaveRequestId(leaveRequestId);
        cc.setReceiverUserId(receiverUserId);
        cc.setReceiverTeacherId(receiverTeacherId);
        cc.setReadFlag(0);
        cc.setRemark(remark);
        leaveRequestCcMapper.insert(cc);
    }

    private void syncAttendanceForApprovedLeave(LeaveRequest leaveRequest) {
        if (leaveRequest.getCourseArrangementId() == null) {
            return;
        }

        LocalDate startDate = leaveRequest.getStartTime().toLocalDate();
        LocalDate endDate = leaveRequest.getEndTime().toLocalDate();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            upsertLeaveAttendance(leaveRequest, current);
            current = current.plusDays(1);
        }
    }

    private void upsertLeaveAttendance(LeaveRequest leaveRequest, LocalDate attendanceDate) {
        Attendance existing = attendanceMapper.selectOne(
                new LambdaQueryWrapper<Attendance>()
                        .eq(Attendance::getStudentId, leaveRequest.getStudentId())
                        .eq(Attendance::getCourseArrangementId, leaveRequest.getCourseArrangementId())
                        .eq(Attendance::getAttendanceDate, attendanceDate)
        );

        if (existing == null) {
            Attendance attendance = new Attendance();
            attendance.setStudentId(leaveRequest.getStudentId());
            attendance.setCourseArrangementId(leaveRequest.getCourseArrangementId());
            attendance.setAttendanceDate(attendanceDate);
            attendance.setStatus(Attendance.Status.LEAVE);
            attendance.setRemark("Auto synced from approved leave request #" + leaveRequest.getId());
            attendanceMapper.insert(attendance);
            return;
        }

        if (existing.getStatus() == Attendance.Status.PRESENT) {
            return;
        }
        existing.setStatus(Attendance.Status.LEAVE);
        if (existing.getRemark() == null || existing.getRemark().isBlank()) {
            existing.setRemark("Auto synced from approved leave request #" + leaveRequest.getId());
        }
        attendanceMapper.updateById(existing);
    }
}
