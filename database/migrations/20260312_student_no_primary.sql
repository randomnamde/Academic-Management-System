ALTER TABLE student
    ADD UNIQUE KEY uk_student_id (id);

ALTER TABLE student
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (student_no);
