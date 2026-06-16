CREATE TABLE IF NOT EXISTS activities (
                                          id BIGSERIAL PRIMARY KEY,
                                          school_id VARCHAR(10) NOT NULL,
    title VARCHAR(300) NOT NULL,
    description TEXT,
    activity_date DATE NOT NULL,
    created_by BIGINT,
    approval_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    visibility_type VARCHAR(50) NOT NULL DEFAULT 'WHOLE_SCHOOL',
    cover_media_id BIGINT,
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS activity_media (
                                              id BIGSERIAL PRIMARY KEY,
                                              school_id VARCHAR(10) NOT NULL,
    activity_id BIGINT NOT NULL,
    file_name VARCHAR(500),
    content_type VARCHAR(100),
    storage_key VARCHAR(1000),
    media_type VARCHAR(20) NOT NULL DEFAULT 'PHOTO',
    thumbnail_key VARCHAR(1000),
    uploaded_by BIGINT,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_activity_media_activity FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS activity_class_visibility (
                                                         id BIGSERIAL PRIMARY KEY,
                                                         school_id VARCHAR(10) NOT NULL,
    activity_id BIGINT NOT NULL,
    class_id BIGINT,
    class_name VARCHAR(100),
    section VARCHAR(50),
    CONSTRAINT fk_activity_class_visibility_activity FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS activity_student_visibility (
                                                           id BIGSERIAL PRIMARY KEY,
                                                           school_id VARCHAR(10) NOT NULL,
    activity_id BIGINT NOT NULL,
    student_id BIGINT,
    student_username VARCHAR(100),
    CONSTRAINT fk_activity_student_visibility_activity FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS activity_approval_history (
                                                         id BIGSERIAL PRIMARY KEY,
                                                         school_id VARCHAR(10) NOT NULL,
    activity_id BIGINT NOT NULL,
    action VARCHAR(30) NOT NULL,
    remarks TEXT,
    action_by BIGINT,
    action_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_activity_approval_history_activity FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_activities_school_status ON activities(school_id, approval_status);
CREATE INDEX IF NOT EXISTS idx_activities_school_date ON activities(school_id, activity_date DESC);
CREATE INDEX IF NOT EXISTS idx_activity_media_activity ON activity_media(activity_id);
CREATE INDEX IF NOT EXISTS idx_activity_class_visibility_activity ON activity_class_visibility(activity_id);
CREATE INDEX IF NOT EXISTS idx_activity_student_visibility_activity ON activity_student_visibility(activity_id);
