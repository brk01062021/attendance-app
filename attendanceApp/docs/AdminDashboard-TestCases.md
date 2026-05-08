# VidyaSetu Admin Dashboard - Test Cases

## Module Scope
This document validates:

- Admin dashboard
- Whole-school attendance
- Class dashboard
- Teacher dashboard
- Subject dashboard
- Date range dashboard
- Teacher operations
- Notifications/admin broadcasts
- Future multi-school support

---

## 1) Admin Dashboard Load

### TC-ADMIN-001: Admin dashboard opens after login
**Expected**
- Admin login routes to Admin Dashboard
- Cards and summary sections visible
- No crash

**Status**
⬜ Pending / Manual

---

## 2) Daily Whole-School Summary

### TC-ADMIN-002: Daily admin attendance dashboard
**API**
`GET /attendance/dashboard/admin?date=2026-05-08`

**Expected**
- totalStudents
- present
- absent
- late
- percentage

**Status**
⬜ Pending / Manual

---

## 3) Class Dashboard

### TC-ADMIN-003: Class-wise dashboard
**API**
`GET /attendance/dashboard/admin/classes?date=2026-05-08`

**Expected**
- Grouped by class-section
- Counts and percentage correct

**Status**
⬜ Pending / Manual

---

## 4) Teacher Dashboard

### TC-ADMIN-004: Teacher-wise dashboard
**API**
`GET /attendance/dashboard/admin/teachers?date=2026-05-08`

**Expected**
- Grouped by teacher
- Teacher name visible
- Counts and percentage correct

**Status**
⬜ Pending / Manual

---

## 5) Subject Dashboard

### TC-ADMIN-005: Subject-wise dashboard
**API**
`GET /attendance/dashboard/admin/subjects?date=2026-05-08`

**Expected**
- Grouped by subject
- Counts and percentage correct

**Status**
⬜ Pending / Manual

---

## 6) Date Range Dashboard

### TC-ADMIN-006: Date range summary
**API**
`GET /attendance/dashboard/admin/date-range?startDate=2026-05-01&endDate=2026-05-08`

**Expected**
- Sorted by date
- Counts and percentage per date

**Status**
⬜ Pending / Manual

---

## 7) Student Attendance Detail

### TC-ADMIN-007: Admin student attendance filter
**API**
`GET /attendance/dashboard/admin/students?className=10&section=A&date=2026-05-08`

**Expected**
- Only matching class/section/date records
- Student ID/name/status/subject/date returned

**Status**
⬜ Pending / Manual

---

## 8) Notification Broadcast

### TC-ADMIN-008: Admin sends school-wide notification
**API**
`POST /notifications/broadcast/school`

**Expected**
- All students receive notification

**Status**
✅ Passed

---

### TC-ADMIN-009: Admin sends class notification
**API**
`POST /notifications/broadcast/class`

**Expected**
- Only selected class receives notification

**Status**
✅ Passed

---

### TC-ADMIN-010: Admin sends section notification
**API**
`POST /notifications/broadcast/section`

**Expected**
- Only selected class-section receives notification

**Status**
✅ Passed

---

### TC-ADMIN-011: Admin sends role notification
**API**
`POST /notifications/broadcast/role`

**Expected**
- Only selected role receives notification

**Status**
✅ Passed

---

## 9) Teacher Leave / Replacement

### TC-ADMIN-012: Admin opens Teacher Leave Planning
**Expected**
- Teacher leave planning screen loads
- Schedule cards visible

**Status**
⬜ Pending / Manual

---

### TC-ADMIN-013: Admin assigns replacement
**Expected**
- Replacement teacher saved
- Status visible in result cards

**Status**
⬜ Pending / Manual

---

## 10) Future Multi-School Support

### TC-ADMIN-014: Admin sees only own school data
**Expected**
- school_id filtering enforced
- Admin cannot access another school

**Status**
⬜ Future / RBAC Required

---

### TC-ADMIN-015: Multi-school dashboard isolation
**Expected**
- Attendance, notifications, users, students filtered by school_id

**Status**
⬜ Future

---

## 11) Regression

### TC-ADMIN-016: Parent dashboard unaffected
**Status**
⬜ Pending / Manual

### TC-ADMIN-017: Teacher dashboard unaffected
**Status**
⬜ Pending / Manual

### TC-ADMIN-018: Student dashboard unaffected
**Status**
⬜ Pending / Manual

---

## Final Status
**Module Status:** MVP Manual Validation Required
