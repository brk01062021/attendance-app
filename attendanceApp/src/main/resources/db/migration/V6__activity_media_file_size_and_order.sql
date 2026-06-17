ALTER TABLE activity_media ADD COLUMN IF NOT EXISTS file_size BIGINT;
ALTER TABLE activity_media ADD COLUMN IF NOT EXISTS display_order INTEGER DEFAULT 0;
CREATE INDEX IF NOT EXISTS idx_activity_media_school_activity_order ON activity_media(school_id, activity_id, display_order, uploaded_at);
