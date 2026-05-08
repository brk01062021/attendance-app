# VidyaSetu Teacher Replacement Module - Test Cases

## Module Scope
This document validates:

- Teacher leave planning
- Planned/unplanned leave
- Replacement options
- Best match / same class / others grouping
- Assign replacement
- Auto assign
- Bulk replacement
- Multi-day replacement workflow
- Admin visibility

---

## 1) Teacher Leave Planning

### TC-REPL-001: Open Teacher Leave Planning screen
**Expected**
- Screen loads
- Date selector visible
- Teacher filter visible
- Schedule cards visible

**Status**
⬜ Pending / Manual

---

### TC-REPL-002: Select one-day leave date
**Expected**
- Schedules for selected date load
- Teacher periods shown correctly

**Status**
⬜ Pending / Manual

---

### TC-REPL-003: Select multi-day leave range
**Expected**
- Date range accepted
- Affected periods grouped by date

**Status**
⬜ Future / In Progress

---

## 2) Mark Leave

### TC-REPL-004: Mark planned leave
**Expected**
- Status changes to PLANNED_LEAVE
- Confirmation modal appears
- Screen resets after apply

**Status**
⬜ Pending / Manual

---

### TC-REPL-005: Mark unplanned leave
**Expected**
- Status changes to UNPLANNED_LEAVE
- Confirmation modal appears
- Screen resets after apply

**Status**
⬜ Pending / Manual

---

### TC-REPL-006: Single-period leave
**Expected**
- Only selected period is marked leave
- Other periods remain available

**Status**
⬜ Pending / Manual

---

## 3) Replacement Options

### TC-REPL-007: Get replacement options
**API**
`GET /teacher-schedules/{id}/replacement-options`

**Expected**
- HTTP 200
- Groups returned:
  - Best Match
  - Same Class
  - Others

**Status**
⬜ Pending / Manual

---

### TC-REPL-008: Best match sorting
**Expected**
- Best available replacement teachers listed first
- Busy/leave teachers excluded

**Status**
⬜ Pending / Manual

---

## 4) Assign Replacement

### TC-REPL-009: Assign single replacement
**Expected**
- Replacement teacher saved
- replacementTeacherId populated
- replacementTeacherName populated
- replacementClass updated if applicable

**Status**
⬜ Pending / Manual

---

### TC-REPL-010: Assign replacement from popup
**Expected**
- Popup closes after success
- Schedule card shows assigned replacement

**Status**
⬜ Pending / Manual

---

## 5) Bulk Replacement

### TC-REPL-011: Bulk assign selected periods
**Expected**
- Multiple selected schedule IDs assigned same replacement
- Response returns assigned count

**Status**
⬜ Pending / Manual

---

### TC-REPL-012: Bulk assign should appear only in Admin Teacher Dashboard result cards
**Expected**
- Bulk assign option should not appear unexpectedly in Teacher Leave Planning screen
- Appears after leave applied without replacement

**Status**
⬜ Pending / Manual

---

## 6) Auto Assign

### TC-REPL-013: Auto assign best matches
**Expected**
- Unassigned periods automatically assigned
- Best match priority used
- Response shows assigned/stillUnassigned counts

**Status**
⬜ Pending / Manual

---

### TC-REPL-014: Auto assign no available teacher
**Expected**
- Period remains unassigned
- stillUnassigned count increments
- No crash

**Status**
⬜ Pending / Manual

---

## 7) Multi-Day Workflow

### TC-REPL-015: Multi-day bulk mark leave
**Expected**
- Leave applied across date range
- Affected periods grouped by date

**Status**
⬜ Future

---

### TC-REPL-016: Assign replacement per period across multi-day leave
**Expected**
- Admin can assign per period
- Existing single-day assignment behavior preserved

**Status**
⬜ Future

---

## 8) Admin Dashboard Integration

### TC-REPL-017: Admin Teacher Dashboard shows leave replacement status
**Expected**
- Assigned and not assigned periods visible
- Sorting by teacher name/status works

**Status**
⬜ Pending / Manual

---

### TC-REPL-018: Replacement audit log
**Expected**
- Who replaced whom
- Date/time
- Admin action user
- Old/new replacement values

**Status**
⬜ Future

---

## 9) Regression

### TC-REPL-019: Attendance unaffected
**Status**
⬜ Pending / Manual

### TC-REPL-020: Notifications unaffected
**Status**
⬜ Pending / Manual

### TC-REPL-021: Teacher dashboard unaffected
**Status**
⬜ Pending / Manual

---

## Final Status
**Module Status:** MVP Core Working / Multi-day Future Enhancement Pending
