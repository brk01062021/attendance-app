# VidyaSetu Parent Module - Test Cases

## Module Scope
This document validates the Parent experience:

- Parent Dashboard
- Attendance drilldown
- Exam results
- Notices
- Notifications
- Reports
- Navigation
- Data isolation

---

## 1) Parent Dashboard

### TC-PARENT-001: Parent dashboard loads after login
**Expected**
- Parent name/student greeting visible
- Premium dark/gold theme preserved
- Cards load without crash

**Status**
⬜ Pending / Manual

---

### TC-PARENT-002: Dashboard card layout
**Expected**
- Cards are aligned
- Text is readable
- No overlap on iPhone screen
- Background/watermark visible correctly

**Status**
⬜ Pending / Manual

---

## 2) Attendance

### TC-PARENT-003: Parent opens attendance card
**Expected**
- Attendance detail screen opens
- Student attendance summary visible

**Status**
⬜ Pending / Manual

---

### TC-PARENT-004: Attendance graphs load
**Expected**
- Present/Absent/Late values displayed
- Graph renders without crash
- Empty data handled gracefully

**Status**
⬜ Pending / Manual

---

## 3) Exam Results

### TC-PARENT-005: Parent opens exam results
**Expected**
- Exam list or results page opens
- Subject-wise marks visible
- Empty result state handled

**Status**
⬜ Pending / Manual

---

### TC-PARENT-006: Exam result detail navigation
**Expected**
- Tapping exam opens detail screen
- Marks/grade/status shown clearly

**Status**
⬜ Pending / Manual

---

## 4) Notices

### TC-PARENT-007: School notices list
**Expected**
- Notices display title/date/message
- Latest notices appear first

**Status**
⬜ Pending / Manual

---

### TC-PARENT-008: Notice detail view
**Expected**
- Full notice content visible
- Back navigation works

**Status**
⬜ Pending / Manual

---

## 5) Notifications

### TC-PARENT-009: Parent notification bell opens notifications page
**Expected**
- Notifications page opens
- Parent/student relevant notifications visible

**Status**
⬜ Pending / Manual

---

### TC-PARENT-010: Mark notification read
**Expected**
- Notification read status changes
- UI visually updates

**Status**
⬜ Pending / Manual

---

### TC-PARENT-011: Mark all notifications read
**Expected**
- All visible notifications marked read
- Badge/count clears if implemented

**Status**
⬜ Pending / Manual

---

## 6) Reports

### TC-PARENT-012: Parent opens reports page
**Expected**
- Reports screen uses gold background
- Attendance/exam report links visible

**Status**
⬜ Pending / Manual

---

### TC-PARENT-013: Download or view report
**Expected**
- Report data loads
- Empty state shown if no records

**Status**
⬜ Pending / Manual

---

## 7) Data Isolation

### TC-PARENT-014: Parent sees only linked student data
**Expected**
- Parent cannot see another student's attendance/results/notices
- Backend should filter by parent/student relation in production

**Status**
⬜ Future / RBAC Required

---

## 8) Regression

### TC-PARENT-015: Parent dashboard unaffected by notification changes
**Status**
⬜ Pending / Manual

### TC-PARENT-016: Parent attendance unaffected by report changes
**Status**
⬜ Pending / Manual

### TC-PARENT-017: Parent notices unaffected by notification updates
**Status**
⬜ Pending / Manual

---

## Final Status
**Module Status:** MVP Manual Validation Required
