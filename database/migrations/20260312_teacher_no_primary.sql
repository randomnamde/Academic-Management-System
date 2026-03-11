-- Promote teacher_no to the teacher table primary key while keeping numeric id as an internal unique key.

START TRANSACTION;

ALTER TABLE teacher
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (teacher_no),
    ADD UNIQUE KEY uk_teacher_id (id);

COMMIT;
