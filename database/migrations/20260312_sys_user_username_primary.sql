-- Migrate sys_user primary key from internal numeric id to username.
-- This script keeps the numeric id as a unique internal column for compatibility,
-- while all business-facing foreign keys point to sys_user(username).

START TRANSACTION;

ALTER TABLE college
    DROP FOREIGN KEY fk_college_admin_user;

ALTER TABLE teacher
    DROP FOREIGN KEY fk_teacher_user;

ALTER TABLE student
    DROP FOREIGN KEY fk_student_user;

ALTER TABLE announcement
    DROP FOREIGN KEY fk_announcement_author;

ALTER TABLE sys_log
    DROP FOREIGN KEY fk_log_user;

ALTER TABLE sys_user_role
    DROP FOREIGN KEY fk_user_role_user;

ALTER TABLE leave_request_approval
    DROP FOREIGN KEY fk_leave_approval_user;

ALTER TABLE leave_request_cc
    DROP FOREIGN KEY fk_leave_cc_user;

ALTER TABLE college
    MODIFY COLUMN admin_user_id VARCHAR(50) NULL;

UPDATE college c
JOIN sys_user u ON c.admin_user_id = CAST(u.id AS CHAR)
SET c.admin_user_id = u.username
WHERE c.admin_user_id IS NOT NULL;

ALTER TABLE teacher
    MODIFY COLUMN user_id VARCHAR(50) NOT NULL;

UPDATE teacher t
JOIN sys_user u ON t.user_id = CAST(u.id AS CHAR)
SET t.user_id = u.username;

ALTER TABLE student
    MODIFY COLUMN user_id VARCHAR(50) NOT NULL;

UPDATE student s
JOIN sys_user u ON s.user_id = CAST(u.id AS CHAR)
SET s.user_id = u.username;

ALTER TABLE announcement
    MODIFY COLUMN author_id VARCHAR(50) NULL;

UPDATE announcement a
JOIN sys_user u ON a.author_id = CAST(u.id AS CHAR)
SET a.author_id = u.username
WHERE a.author_id IS NOT NULL;

ALTER TABLE sys_log
    MODIFY COLUMN user_id VARCHAR(50) NULL;

UPDATE sys_log l
JOIN sys_user u ON l.user_id = CAST(u.id AS CHAR)
SET l.user_id = u.username
WHERE l.user_id IS NOT NULL;

ALTER TABLE sys_user_role
    MODIFY COLUMN user_id VARCHAR(50) NOT NULL;

UPDATE sys_user_role ur
JOIN sys_user u ON ur.user_id = CAST(u.id AS CHAR)
SET ur.user_id = u.username;

ALTER TABLE leave_request_approval
    MODIFY COLUMN approver_user_id VARCHAR(50) NULL;

UPDATE leave_request_approval lra
JOIN sys_user u ON lra.approver_user_id = CAST(u.id AS CHAR)
SET lra.approver_user_id = u.username
WHERE lra.approver_user_id IS NOT NULL;

ALTER TABLE leave_request_cc
    MODIFY COLUMN receiver_user_id VARCHAR(50) NOT NULL;

UPDATE leave_request_cc lrc
JOIN sys_user u ON lrc.receiver_user_id = CAST(u.id AS CHAR)
SET lrc.receiver_user_id = u.username;

ALTER TABLE sys_user
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (username),
    ADD UNIQUE KEY uk_sys_user_id (id);

ALTER TABLE college
    ADD CONSTRAINT fk_college_admin_user FOREIGN KEY (admin_user_id) REFERENCES sys_user(username) ON DELETE SET NULL;

ALTER TABLE teacher
    ADD CONSTRAINT fk_teacher_user FOREIGN KEY (user_id) REFERENCES sys_user(username) ON DELETE CASCADE;

ALTER TABLE student
    ADD CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES sys_user(username) ON DELETE CASCADE;

ALTER TABLE announcement
    ADD CONSTRAINT fk_announcement_author FOREIGN KEY (author_id) REFERENCES sys_user(username) ON DELETE SET NULL;

ALTER TABLE sys_log
    ADD CONSTRAINT fk_log_user FOREIGN KEY (user_id) REFERENCES sys_user(username) ON DELETE SET NULL;

ALTER TABLE sys_user_role
    ADD CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(username) ON DELETE CASCADE;

ALTER TABLE leave_request_approval
    ADD CONSTRAINT fk_leave_approval_user FOREIGN KEY (approver_user_id) REFERENCES sys_user(username) ON DELETE SET NULL;

ALTER TABLE leave_request_cc
    ADD CONSTRAINT fk_leave_cc_user FOREIGN KEY (receiver_user_id) REFERENCES sys_user(username) ON DELETE CASCADE;

COMMIT;
