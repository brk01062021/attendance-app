# VidyaSetu Reports Module - Test Cases

## Module Scope
This document validates:

- Attendance reports
- Monthly reports
- Parent/student report views
- Admin reports
- Report notifications
- Future downloads

---

## 1) Attendance Report

### TC-REPORT-001: Class-section attendance report
**API**
`GET /attendance/report?className=10&section=A`

**Expected**
- HTTP 200
- Only selected class-section
- Each student has totalDays, presentDays, absentDays

**Status**
⬜ Pending / Manual

---

### TC-REPORT-002: All-student attendance report
**API**
`GET /attendance/report`

**Expected**
- HTTP 200
- All students included
- Counts populated

**Status**
⬜ Pending / Manual

---

## 2) Monthly Attendance Report

### TC-REPORT-003: Monthly report for May 2026
**API**
`GET /attendance/report/monthly?className=10&section=A&year=2026&month=5`

**Expected**
- HTTP 200
- Only May 2026 records included
- Only Class 10 Section A students

**Status**
⬜ Pending / Manual

---

### TC-REPORT-004: Monthly report with no attendance records
**Expected**
- HTTP 200
- Students returned with zero counts or empty report depending implementation
- No crash

**Status**
⬜ Pending / Manual

---

## 3) Parent Reports

### TC-REPORT-005: Parent opens reports screen
**Expected**
- Reports page opens
- Gold theme preserved
- Student report data visible

**Status**
⬜ Pending / Manual

---

### TC-REPORT-006: Parent views attendance report
**Expected**
- Parent sees only linked student
- Attendance percentage shown

**Status**
⬜ Pending / Manual

---

### TC-REPORT-007: Parent views exam report
**Expected**
- Exam results report visible
- Empty state handled

**Status**
⬜ Pending / Manual

---

## 4) Student Reports

### TC-REPORT-008: Student opens reports screen
**Expected**
- Reports page opens
- Gold background visible
- Student sees own reports only

**Status**
⬜ Pending / Manual

---

## 5) Admin Reports

### TC-REPORT-009: Admin class-wise report
**Expected**
- Admin can filter by class and section
- Counts and percentages visible

**Status**
⬜ Pending / Manual

---

### TC-REPORT-010: Admin date-range report
**Expected**
- Admin can view trends by date range
- Results sorted by date

**Status**
⬜ Pending / Manual

---

## 6) Report Notifications

### TC-REPORT-011: Weekly report notification
**API**
`POST /attendance/notify/weekly?className=10&section=A`

**Expected**
- Notifications created for students
- Type = ATTENDANCE_REPORT

**Status**
✅ Passed

---

### TC-REPORT-012: Monthly report notification
**API**
`POST /attendance/notify/monthly?className=10&section=A&year=2026&month=5`

**Expected**
- Notifications created for students
- Type = ATTENDANCE_REPORT

**Status**
✅ Passed

---

## 7) Future Download Support

### TC-REPORT-013: Download attendance report PDF
**Status**
⬜ Future

### TC-REPORT-014: Download exam report PDF
**Status**
⬜ Future

### TC-REPORT-015: Export Excel report
**Status**
⬜ Future

---

## 8) Regression

### TC-REPORT-016: Attendance submit unaffected
**Status**
⬜ Pending / Manual

### TC-REPORT-017: Notifications unaffected
**Status**
⬜ Pending / Manual

### TC-REPORT-018: Parent/student dashboards unaffected
**Status**
⬜ Pending / Manual

---

## Final Status
**Module Status:** MVP Manual Validation Required
