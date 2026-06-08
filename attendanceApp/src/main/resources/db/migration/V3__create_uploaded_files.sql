CREATE TABLE IF NOT EXISTS uploaded_files (
                                              id BIGSERIAL PRIMARY KEY,
                                              school_id VARCHAR(20) NOT NULL,
    module VARCHAR(80) NOT NULL,
    storage_provider VARCHAR(40) NOT NULL,
    bucket VARCHAR(255),
    storage_key VARCHAR(1000) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(255),
    size_bytes BIGINT,
    uploaded_by VARCHAR(120),
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_uploaded_files_school_id
    ON uploaded_files (school_id);

CREATE INDEX IF NOT EXISTS idx_uploaded_files_school_module
    ON uploaded_files (school_id, module);

CREATE INDEX IF NOT EXISTS idx_uploaded_files_created_at
    ON uploaded_files (created_at);