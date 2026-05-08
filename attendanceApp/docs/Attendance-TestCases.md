# VidyaSetu Attendance Module - Test Cases

## Module Scope
This document validates the core attendance flow for VidyaSetu:

- Load students by class and section
- Submit single attendance
- Submit bulk attendance
- Update existing attendance
- Teacher filtered attendance
- Date-based attendance
- Attendance summary
- Monthly attendance report
- Admin dashboards
- Teacher dashboards
- Regression checks

---

## 1) Load Students

### TC-ATT-001: Load students by class and section
**API**
`GET /students?className=10&section=A`

**Expected**
- HTTP 200
- Only Class 10 Section A students returned
- Each student contains id, name, className, section

**Status**
⬜ Pending / Manual

---

## 2) Single Attendance Submit

### TC-ATT-002: Mark one student present
**API**
`POST /attendance`

**Request**
```json
{
  "studentId": 1,
  "attendanceDate": "2026-05-08",
  "teacherId": 1,
  "teacherName": "Teacher One",
  "subjectName": "Mathematics",
  "className": "10",
  "section": "A",
  "status": "PRESENT"
}
```

**Expected**
- HTTP 200
- Attendance saved
- Status = PRESENT
- Existing same student/date/teacher/subject/class/section record is updated, not duplicated

**Status**
⬜ Pending / Manual

---

### TC-ATT-003: Mark one student absent
**Expected**
- HTTP 200
- Status = ABSENT

**Status**
⬜ Pending / Manual

---

### TC-ATT-004: Mark one student late
**Expected**
- HTTP 200
- Status = LATE

**Status**
⬜ Pending / Manual

---

### TC-ATT-005: Invalid attendance status
**Request status**
`INVALID`

**Expected**
- HTTP 400 or handled error
- Error message should mention valid values: PRESENT, ABSENT, LATE
- No attendance row should be saved

**Status**
⬜ Pending / Manual

---

## 3) Bulk Attendance Submit

### TC-ATT-006: Submit bulk attendance for class
**API**
`POST /attendance/bulk`

**Expected**
- HTTP 200
- All student attendance rows saved
- Present/Absent/Late values persisted
- No duplicate rows for same student/date/teacher/subject/class/section

**Status**
⬜ Pending / Manual

---

### TC-ATT-007: Bulk submit with one invalid student ID
**Expected**
- Error returned
- Invalid student should not be saved
- Verify whether partial save behavior is acceptable for MVP

**Status**
⬜ Pending / Manual

---

## 4) Attendance Fetch

### TC-ATT-008: Fetch all attendance
**API**
`GET /attendance`

**Expected**
- HTTP 200
- List of attendance records returned

**Status**
⬜ Pending / Manual

---

### TC-ATT-009: Fetch filtered teacher attendance
**API**
`GET /attendance?teacherId=1&subjectName=Mathematics&className=10&section=A&attendanceDate=2026-05-08`

**Expected**
- HTTP 200
- Only matching teacher/subject/class/section/date records returned

**Status**
⬜ Pending / Manual

---

### TC-ATT-010: Fetch attendance by date
**API**
`GET /attendance/date?date=2026-05-08`

**Expected**
- HTTP 200
- Only matching date records returned

**Status**
⬜ Pending / Manual

---

### TC-ATT-011: Fetch attendance by student
**API**
`GET /attendance/student/1`

**Expected**
- HTTP 200
- Only student ID 1 attendance returned

**Status**
⬜ Pending / Manual

---

## 5) Attendance Summary

### TC-ATT-012: Overall attendance summary
**API**
`GET /attendance/summary`

**Expected**
- HTTP 200
- Response contains present, absent, late counts

**Status**
⬜ Pending / Manual

---

### TC-ATT-013: Date-wise attendance summary
**API**
`GET /attendance/summary/date?date=2026-05-08`

**Expected**
- HTTP 200
- Counts only for selected date

**Status**
⬜ Pending / Manual

---

## 6) Reports

### TC-ATT-014: Attendance report by class and section
**API**
`GET /attendance/report?className=10&section=A`

**Expected**
- HTTP 200
- Report contains totalDays, presentDays, absentDays
- Only Class 10 Section A students returned

**Status**
⬜ Pending / Manual

---

### TC-ATT-015: Monthly attendance report
**API**
`GET /attendance/report/monthly?className=10&section=A&year=2026&month=5`

**Expected**
- HTTP 200
- Only May 2026 records considered
- presentDays includes PRESENT + LATE
- absentDays includes ABSENT

**Status**
⬜ Pending / Manual

---

## 7) Admin Dashboards

### TC-ATT-016: Admin daily dashboard
**API**
`GET /attendance/dashboard/admin?date=2026-05-08`

**Expected**
- HTTP 200
- totalStudents populated
- present, absent, late populated
- percentage calculated correctly

**Status**
⬜ Pending / Manual

---

### TC-ATT-017: Admin class dashboard
**API**
`GET /attendance/dashboard/admin/classes?date=2026-05-08`

**Expected**
- HTTP 200
- Grouped by class-section
- Percentage calculated per group

**Status**
⬜ Pending / Manual

---

### TC-ATT-018: Admin teacher dashboard
**API**
`GET /attendance/dashboard/admin/teachers?date=2026-05-08`

**Expected**
- HTTP 200
- Grouped by teacherId
- Teacher name and attendance counts shown

**Status**
⬜ Pending / Manual

---

### TC-ATT-019: Admin subject dashboard
**API**
`GET /attendance/dashboard/admin/subjects?date=2026-05-08`

**Expected**
- HTTP 200
- Grouped by subjectName
- Counts and percentages shown

**Status**
⬜ Pending / Manual

---

### TC-ATT-020: Admin date range dashboard
**API**
`GET /attendance/dashboard/admin/date-range?startDate=2026-05-01&endDate=2026-05-08`

**Expected**
- HTTP 200
- Results sorted by date
- Each date has counts and percentage

**Status**
⬜ Pending / Manual

---

## 8) Teacher Dashboard

### TC-ATT-021: Teacher dashboard by date
**API**
`GET /attendance/dashboard/teacher?teacherId=1&date=2026-05-08`

**Expected**
- HTTP 200
- teacherId and teacherName populated
- Counts and percentage shown

**Status**
⬜ Pending / Manual

---

### TC-ATT-022: Teacher class dashboard
**API**
`GET /attendance/dashboard/teacher/classes?teacherId=1&date=2026-05-08`

**Expected**
- HTTP 200
- Grouped by class-section-subject
- Counts and percentage shown

**Status**
⬜ Pending / Manual

---

## 9) UI Validation

### TC-ATT-023: Teacher attendance flow
**Steps**
1. Login as Teacher
2. Open Teacher Dashboard
3. Tap Take Attendance
4. Select subject/class/section
5. Mark statuses
6. Submit

**Expected**
- Submit succeeds
- Success message shown
- User returns to Teacher Dashboard

**Status**
⬜ Pending / Manual

---

### TC-ATT-024: Search student in attendance screen
**Expected**
- Search filters student cards
- Clear button resets search
- Bulk present/absent still works after search reset

**Status**
⬜ Pending / Manual

---

## 10) Regression

### TC-ATT-025: Notification module unaffected
**Expected**
- Notification page still loads
- Read/unread still works

**Status**
⬜ Pending / Manual

---

### TC-ATT-026: Teacher replacement module unaffected
**Expected**
- Replacement screens and backend APIs still work

**Status**
⬜ Pending / Manual

---

## Final Status
**Module Status:** In Progress / MVP Validation Required
