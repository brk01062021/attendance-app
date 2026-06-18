-- Day 4 hardening: workbook commit/recommit must be idempotent for generated role users.
-- Older recommit builds could insert duplicate TEACHER/STUDENT/PARENT app_users rows.
-- Keep the newest row for each tenant + role + username and prevent future duplicates.
DELETE FROM app_users older
    USING app_users newer
WHERE older.id < newer.id
  AND UPPER(COALESCE(older.school_code, '')) = UPPER(COALESCE(newer.school_code, ''))
  AND UPPER(COALESCE(older.role, '')) = UPPER(COALESCE(newer.role, ''))
  AND UPPER(COALESCE(older.username, '')) = UPPER(COALESCE(newer.username, ''));

CREATE UNIQUE INDEX IF NOT EXISTS ux_app_users_school_code_role_username
    ON app_users (
    UPPER(COALESCE(school_code, '')),
    UPPER(COALESCE(role, '')),
    UPPER(COALESCE(username, ''))
    );
