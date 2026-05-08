# VidyaSetu Teacher Dashboard - Test Cases

## Module Scope
This document validates:

- Teacher login routing
- Teacher dashboard layout
- Take Attendance flow
- Today attendance summary
- Notifications
- Menu navigation
- Replacement menu placeholder

---

## 1) Teacher Dashboard Load

### TC-TEACHER-DASH-001: Teacher dashboard opens after login
**Expected**
- Login as teacher routes to Teacher Dashboard
- Premium dark theme visible
- Teacher name/greeting visible

**Status**
⬜ Pending / Manual

---

### TC-TEACHER-DASH-002: Dashboard card layout
**Expected**
- Cards aligned
- Text readable
- No splash background loss
- No overlap on iPhone

**Status**
⬜ Pending / Manual

---

## 2) Take Attendance

### TC-TEACHER-DASH-003: Tap Take Attendance
**Expected**
- Navigates to Load Students page
- Does not directly open submit page

**Status**
⬜ Pending / Manual

---

### TC-TEACHER-DASH-004: Select subject/class/section
**Expected**
- Dropdowns populate correctly
- Submit/continue opens attendance page

**Status**
⬜ Pending / Manual

---

### TC-TEACHER-DASH-005: Submit attendance returns to Teacher Dashboard
**Expected**
- Submit success
- Navigates back to Teacher Dashboard
- No duplicate submit

**Status**
⬜ Pending / Manual

---

## 3) Smart Attendance UX

### TC-TEACHER-DASH-006: Search student by name
**Expected**
- Student list filters by typed name
- No match state shown

**Status**
⬜ Pending / Manual

---

### TC-TEACHER-DASH-007: Clear search button
**Expected**
- Close button appears after typing
- Clear button resets list

**Status**
⬜ Pending / Manual

---

### TC-TEACHER-DASH-008: Bulk present
**Expected**
- All visible/eligible students marked present
- Summary count updates

**Status**
⬜ Pending / Manual

---

### TC-TEACHER-DASH-009: Attendance summary before submit
**Expected**
- Present count shown
- Late count shown
- Absent count shown
- Counts update live

**Status**
⬜ Pending / Manual

---

## 4) Notifications

### TC-TEACHER-DASH-010: Notification bell opens page
**Expected**
- Notifications page opens
- Teacher role notifications visible

**Status**
⬜ Pending / Manual

---

### TC-TEACHER-DASH-011: Teacher role broadcast visible
**Expected**
- Staff meeting notification visible for teacher user
- Type = SCHOOL_ALERT

**Status**
⬜ Pending / Manual

---

## 5) Replacement Menu

### TC-TEACHER-DASH-012: Replacement menu item visible
**Expected**
- Teacher menu contains replacement option
- Navigation does not crash

**Status**
⬜ Pending / Manual

---

## 6) Regression

### TC-TEACHER-DASH-013: Splash backgrounds preserved
**Expected**
- Dark/gold backgrounds not lost after code updates

**Status**
⬜ Pending / Manual

---

### TC-TEACHER-DASH-014: Parent/student dashboards unaffected
**Status**
⬜ Pending / Manual

---

## Final Status
**Module Status:** MVP Manual Validation Required
