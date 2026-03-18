-- Migrate FK references from teacher(id) to teacher(teacher_no)
START TRANSACTION;

ALTER TABLE class ADD COLUMN teacher_no_tmp VARCHAR(20) NULL;
UPDATE class c JOIN teacher t ON c.teacher_id = t.id SET c.teacher_no_tmp = t.teacher_no WHERE c.teacher_id IS NOT NULL;
ALTER TABLE class DROP FOREIGN KEY fk_class_teacher;
ALTER TABLE class DROP COLUMN teacher_id;
ALTER TABLE class CHANGE COLUMN teacher_no_tmp teacher_no VARCHAR(20) NULL;
ALTER TABLE class ADD CONSTRAINT fk_class_teacher FOREIGN KEY (teacher_no) REFERENCES teacher(teacher_no) ON DELETE SET NULL;

ALTER TABLE course_arrangement ADD COLUMN teacher_no_tmp VARCHAR(20) NULL;
UPDATE course_arrangement ca JOIN teacher t ON ca.teacher_id = t.id SET ca.teacher_no_tmp = t.teacher_no WHERE ca.teacher_id IS NOT NULL;
ALTER TABLE course_arrangement DROP FOREIGN KEY fk_arrangement_teacher;
ALTER TABLE course_arrangement DROP COLUMN teacher_id;
ALTER TABLE course_arrangement CHANGE COLUMN teacher_no_tmp teacher_no VARCHAR(20) NOT NULL;
ALTER TABLE course_arrangement ADD CONSTRAINT fk_arrangement_teacher FOREIGN KEY (teacher_no) REFERENCES teacher(teacher_no) ON DELETE CASCADE;

ALTER TABLE leave_request ADD COLUMN approver_no_tmp VARCHAR(20) NULL;
UPDATE leave_request lr JOIN teacher t ON lr.approver_id = t.id SET lr.approver_no_tmp = t.teacher_no WHERE lr.approver_id IS NOT NULL;
ALTER TABLE leave_request DROP FOREIGN KEY fk_leave_approver;
ALTER TABLE leave_request DROP COLUMN approver_id;
ALTER TABLE leave_request CHANGE COLUMN approver_no_tmp approver_no VARCHAR(20) NULL;
ALTER TABLE leave_request ADD CONSTRAINT fk_leave_approver FOREIGN KEY (approver_no) REFERENCES teacher(teacher_no) ON DELETE SET NULL;

ALTER TABLE leave_request_approval ADD COLUMN approver_teacher_no_tmp VARCHAR(20) NULL;
UPDATE leave_request_approval lra JOIN teacher t ON lra.approver_teacher_id = t.id SET lra.approver_teacher_no_tmp = t.teacher_no WHERE lra.approver_teacher_id IS NOT NULL;
ALTER TABLE leave_request_approval DROP FOREIGN KEY fk_leave_approval_teacher;
ALTER TABLE leave_request_approval DROP COLUMN approver_teacher_id;
ALTER TABLE leave_request_approval CHANGE COLUMN approver_teacher_no_tmp approver_teacher_no VARCHAR(20) NULL;
ALTER TABLE leave_request_approval ADD CONSTRAINT fk_leave_approval_teacher FOREIGN KEY (approver_teacher_no) REFERENCES teacher(teacher_no) ON DELETE SET NULL;

ALTER TABLE leave_request_cc ADD COLUMN receiver_teacher_no_tmp VARCHAR(20) NULL;
UPDATE leave_request_cc lrc JOIN teacher t ON lrc.receiver_teacher_id = t.id SET lrc.receiver_teacher_no_tmp = t.teacher_no WHERE lrc.receiver_teacher_id IS NOT NULL;
ALTER TABLE leave_request_cc DROP FOREIGN KEY fk_leave_cc_teacher;
ALTER TABLE leave_request_cc DROP COLUMN receiver_teacher_id;
ALTER TABLE leave_request_cc CHANGE COLUMN receiver_teacher_no_tmp receiver_teacher_no VARCHAR(20) NULL;
ALTER TABLE leave_request_cc ADD CONSTRAINT fk_leave_cc_teacher FOREIGN KEY (receiver_teacher_no) REFERENCES teacher(teacher_no) ON DELETE SET NULL;

COMMIT;
