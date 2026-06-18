CREATE TABLE IF NOT EXISTS fee_reminder_uploads (
                                                    id BIGSERIAL PRIMARY KEY,
                                                    school_id VARCHAR(4) NOT NULL,
    original_filename VARCHAR(255),
    uploaded_by VARCHAR(255),
    status VARCHAR(40) DEFAULT 'PREVIEW_READY',
    total_rows INT DEFAULT 0,
    ready_rows INT DEFAULT 0,
    invalid_rows INT DEFAULT 0,
    missing_student_rows INT DEFAULT 0,
    missing_parent_mapping_rows INT DEFAULT 0,
    sent_rows INT DEFAULT 0,
    failed_rows INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP NULL
    );

CREATE TABLE IF NOT EXISTS fee_reminder_rows (
                                                 id BIGSERIAL PRIMARY KEY,
                                                 upload_id BIGINT NOT NULL,
                                                 school_id VARCHAR(4) NOT NULL,
    row_number INT,
    student_id VARCHAR(120),
    student_name VARCHAR(255),
    class_name VARCHAR(80),
    section VARCHAR(80),
    pending_amount NUMERIC(12,2),
    due_date DATE,
    remarks TEXT,
    mapped_student_db_id BIGINT,
    mapped_parent_user_ids TEXT,
    mapped_parent_names TEXT,
    status VARCHAR(40),
    validation_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS fee_reminder_history (
                                                    id BIGSERIAL PRIMARY KEY,
                                                    upload_id BIGINT,
                                                    row_id BIGINT,
                                                    school_id VARCHAR(4) NOT NULL,
    student_db_id BIGINT,
    student_id VARCHAR(120),
    student_name VARCHAR(255),
    class_name VARCHAR(80),
    section VARCHAR(80),
    parent_user_id BIGINT,
    parent_name VARCHAR(255),
    pending_amount NUMERIC(12,2),
    due_date DATE,
    remarks TEXT,
    status VARCHAR(40),
    channel VARCHAR(40),
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_fee_upload_school_created ON fee_reminder_uploads (school_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_fee_row_upload ON fee_reminder_rows (upload_id);
CREATE INDEX IF NOT EXISTS idx_fee_row_status ON fee_reminder_rows (status);
CREATE INDEX IF NOT EXISTS idx_fee_history_school_sent ON fee_reminder_history (school_id, sent_at DESC);
CREATE INDEX IF NOT EXISTS idx_fee_history_parent ON fee_reminder_history (parent_user_id);
