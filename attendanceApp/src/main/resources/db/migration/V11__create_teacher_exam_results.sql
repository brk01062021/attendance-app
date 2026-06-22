CREATE TABLE IF NOT EXISTS teacher_exam_results (
                                                    id BIGSERIAL PRIMARY KEY,
                                                    school_id VARCHAR(64),
    teacher_id BIGINT,
    teacher_name VARCHAR(255),
    class_name VARCHAR(64),
    section VARCHAR(64),
    subject_name VARCHAR(255),
    exam_name VARCHAR(255),
    max_marks INTEGER,
    student_id BIGINT,
    student_name VARCHAR(255),
    student_identifier VARCHAR(255),
    marks DOUBLE PRECISION,
    status VARCHAR(32),
    saved_at TIMESTAMP,
    submitted_at TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_teacher_exam_results_lookup
    ON teacher_exam_results (school_id, teacher_id, class_name, section, subject_name, exam_name, student_id);
