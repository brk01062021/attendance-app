ALTER TABLE teacher_schedule ADD COLUMN IF NOT EXISTS school_id VARCHAR(20);
ALTER TABLE teacher_schedule ADD COLUMN IF NOT EXISTS academic_year VARCHAR(20);
ALTER TABLE teacher_schedule ADD COLUMN IF NOT EXISTS import_batch_id VARCHAR(50);
ALTER TABLE teacher_schedule ADD COLUMN IF NOT EXISTS source_type VARCHAR(40);
ALTER TABLE teacher_schedule ADD COLUMN IF NOT EXISTS active_timetable BOOLEAN DEFAULT FALSE;
ALTER TABLE teacher_schedule ADD COLUMN IF NOT EXISTS published_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_teacher_schedule_active_school
    ON teacher_schedule (school_id, active_timetable);

CREATE INDEX IF NOT EXISTS idx_teacher_schedule_import_batch
    ON teacher_schedule (school_id, import_batch_id);
