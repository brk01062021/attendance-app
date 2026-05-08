# VidyaSetu Student Module - Test Cases

## Module Scope
This document validates:

- Student Dashboard
- Attendance
- Exam results
- Reports
- Notifications
- Menu navigation
- Future student auth model

---

## 1) Student Dashboard

### TC-STUDENT-001: Student dashboard loads after login
**Expected**
- Student dashboard opens
- Student greeting visible
- Header layout correct
- Menu and alert buttons visible

**Status**
⬜ Pending / Manual

---

### TC-STUDENT-002: Dashboard visual theme
**Expected**
- Dark premium home style preserved
- Gold internal pages used where expected
- Cards readable and not oversized

**Status**
⬜ Pending / Manual

---

## 2) Dashboard Cards

### TC-STUDENT-003: Attendance card navigation
**Expected**
- Tapping Attendance opens attendance details

**Status**
⬜ Pending / Manual

---

### TC-STUDENT-004: Exam Results should not duplicate as dashboard card if moved to menu
**Expected**
- Exam Results is available from menu
- Dashboard card layout remains clean

**Status**
⬜ Pending / Manual

---

## 3) Student Menu

### TC-STUDENT-005: Menu opens
**Expected**
- Menu opens from top-left
- Items visible
- No duplicate Attendance item if dashboard card exists

**Status**
⬜ Pending / Manual

---

### TC-STUDENT-006: Exam Results menu item
**Expected**
- Opens exam results page
- Page uses splash-gold background

**Status**
⬜ Pending / Manual

---

### TC-STUDENT-007: Reports menu item
**Expected**
- Opens reports page
- Page uses splash-gold background

**Status**
⬜ Pending / Manual

---

## 4) Attendance

### TC-STUDENT-008: Student attendance summary
**Expected**
- Present/Absent/Late counts visible
- Percentage calculated correctly
- Empty state handled

**Status**
⬜ Pending / Manual

---

## 5) Exam Results

### TC-STUDENT-009: Student exam results list
**Expected**
- Results display by exam/subject
- Empty state handled

**Status**
⬜ Pending / Manual

---

### TC-STUDENT-010: Student exam result detail
**Expected**
- Detail screen opens
- Marks/grade/status visible

**Status**
⬜ Pending / Manual

---

## 6) Notifications

### TC-STUDENT-011: Student alert icon opens notification page
**Expected**
- Notifications page opens
- Student notifications visible

**Status**
⬜ Pending / Manual

---

### TC-STUDENT-012: Attendance notification visible
**Expected**
- Weekly/monthly attendance notifications appear
- Type = ATTENDANCE_REPORT

**Status**
⬜ Pending / Manual

---

### TC-STUDENT-013: Exam published notification visible
**Expected**
- Exam result notification appears
- Type = EXAM_RESULT

**Status**
⬜ Pending / Manual

---

## 7) Future Student Authentication

### TC-STUDENT-014: First login using admission number + DOB
**Status**
⬜ Future

### TC-STUDENT-015: PIN setup after first login
**Status**
⬜ Future

### TC-STUDENT-016: Future login using admission number + PIN
**Status**
⬜ Future

### TC-STUDENT-017: Forgot PIN using admission number + DOB
**Status**
⬜ Future

---

## 8) Regression

### TC-STUDENT-018: Student dashboard unaffected by notification updates
**Status**
⬜ Pending / Manual

### TC-STUDENT-019: Student attendance unaffected by report changes
**Status**
⬜ Pending / Manual

---

## Final Status
**Module Status:** MVP Manual Validation Required
