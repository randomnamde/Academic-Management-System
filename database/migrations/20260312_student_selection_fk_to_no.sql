ALTER TABLE student_course_selection
    DROP FOREIGN KEY fk_selection_student,
    MODIFY COLUMN student_id VARCHAR(20) NOT NULL;

UPDATE student_course_selection sc
JOIN student st ON sc.student_id = st.id
SET sc.student_id = st.student_no;

ALTER TABLE student_course_selection
    ADD CONSTRAINT fk_selection_student FOREIGN KEY (student_id) REFERENCES student(student_no) ON DELETE CASCADE;
