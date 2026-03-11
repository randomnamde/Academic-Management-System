ALTER TABLE score
    DROP FOREIGN KEY fk_score_student,
    MODIFY COLUMN student_id VARCHAR(20) NOT NULL;

UPDATE score sc
JOIN student st ON sc.student_id = st.id
SET sc.student_id = st.student_no;

ALTER TABLE score
    ADD CONSTRAINT fk_score_student FOREIGN KEY (student_id) REFERENCES student(student_no) ON DELETE CASCADE;

ALTER TABLE attendance
    DROP FOREIGN KEY fk_attendance_student,
    MODIFY COLUMN student_id VARCHAR(20) NOT NULL;

UPDATE attendance a
JOIN student st ON a.student_id = st.id
SET a.student_id = st.student_no;

ALTER TABLE attendance
    ADD CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES student(student_no) ON DELETE CASCADE;

ALTER TABLE leave_request
    DROP FOREIGN KEY fk_leave_student,
    MODIFY COLUMN student_id VARCHAR(20) NOT NULL;

UPDATE leave_request lr
JOIN student st ON lr.student_id = st.id
SET lr.student_id = st.student_no;

ALTER TABLE leave_request
    ADD CONSTRAINT fk_leave_student FOREIGN KEY (student_id) REFERENCES student(student_no) ON DELETE CASCADE;
