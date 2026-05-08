# VidyaSetu Notification Module - Test Cases

## Module Scope
This document validates the notification module for VidyaSetu covering:

- Single notification
- Bulk notification
- School-wide broadcast
- Class-wide broadcast
- Section-wide broadcast
- Role-based broadcast
- Weekly attendance notification
- Monthly attendance notification
- Fetch notifications
- Fetch unread notifications
- Mark read / unread
- Mark all read
- Regression validation

---

## 1) Single Notification

### TC-NOTIF-001: Create single notification

**API**
`POST /notifications`

**Request**
```json
{
  "userId": 1,
  "role": "STUDENT",
  "title": "Test Notification",
  "message": "This is a test notification.",
  "type": "SCHOOL_ALERT"
}
```

**Expected**
- HTTP 200
- Notification saved
- `read = false`
- `createdAt` populated
- `userId = 1`
- `role = STUDENT`

**Status**
✅ Pass

---

## 2) Bulk Notification

### TC-NOTIF-002: Create bulk notifications

**API**
`POST /notifications/bulk`

**Request**
```json
{
  "userIds": [1, 2, 3],
  "role": "STUDENT",
  "title": "Bulk Notice",
  "message": "Bulk notification test.",
  "type": "SCHOOL_ALERT"
}
```

**Expected**
- HTTP 200
- 3 notifications created
- `read = false`
- same title/message/type

**Status**
✅ Pass

---

## 3) School-wide Broadcast

### TC-NOTIF-003: Broadcast school-wide alert

**API**
`POST /notifications/broadcast/school`

**Request**
```json
{
  "title": "School Holiday",
  "message": "School will remain closed tomorrow.",
  "type": "SCHOOL_ALERT"
}
```

**Expected**
- HTTP 200
- One notification per student
- Role = STUDENT
- `read = false`
- class/section populated correctly

**Validation Result**
- IDs created: 6–22
- Multiple classes covered

**Status**
✅ Pass

---

## 4) Class-wide Broadcast

### TC-NOTIF-004: Broadcast to class

**API**
`POST /notifications/broadcast/class`

**Request**
```json
{
  "className": "10",
  "title": "Exam Results Published",
  "message": "Class 10 results are available.",
  "type": "EXAM_RESULT"
}
```

**Expected**
- HTTP 200
- Only Class 10 students
- Section A + B included
- Role = STUDENT

**Validation Result**
- IDs created: 23–31
- Only class 10

**Status**
✅ Pass

---

## 5) Section-wide Broadcast

### TC-NOTIF-005: Broadcast to section

**API**
`POST /notifications/broadcast/section`

**Request**
```json
{
  "className": "10",
  "section": "A",
  "title": "Attendance Reminder",
  "message": "Weekly attendance summary published.",
  "type": "ATTENDANCE_REPORT"
}
```

**Expected**
- HTTP 200
- Only Class 10 Section A
- Role = STUDENT

**Validation Result**
- IDs created: 32–36
- Only section A

**Status**
✅ Pass

---

## 6) Role-Based Broadcast

### TC-NOTIF-006: Broadcast to TEACHER role

**API**
`POST /notifications/broadcast/role`

**Request**
```json
{
  "role": "TEACHER",
  "title": "Staff Meeting",
  "message": "All teachers have a staff meeting tomorrow at 9 AM.",
  "type": "SCHOOL_ALERT"
}
```

**Expected**
- HTTP 200
- Only TEACHER users
- Role saved = TEACHER

**Validation Result**
- IDs created: 37–39
- 3 teacher users notified

**Status**
✅ Pass

---

## 7) Weekly Attendance Notification

### TC-NOTIF-007: Weekly report notification

**API**
`POST /attendance/notify/weekly?className=10&section=A`

**Expected**
- HTTP 200
- Only Class 10 Section A students
- Title = Weekly Attendance Report
- Type = ATTENDANCE_REPORT

**Validation Result**
- IDs created: 40–44

**Status**
✅ Pass

---

## 8) Monthly Attendance Notification

### TC-NOTIF-008: Monthly report notification

**API**
`POST /attendance/notify/monthly?className=10&section=A&year=2026&month=5`

**Expected**
- HTTP 200
- Only Class 10 Section A
- Title = Monthly Attendance Report
- Type = ATTENDANCE_REPORT

**Validation Result**
- IDs created: 45–49

**Status**
✅ Pass

---

## 9) Fetch Notifications

### TC-NOTIF-009: Fetch notifications

**API**
`GET /notifications?userId=1&role=STUDENT`

**Expected**
- HTTP 200
- latest-first ordering
- correct user only

**Status**
✅ Pass

---

## 10) Fetch Unread Notifications

### TC-NOTIF-010: Fetch unread notifications

**API**
`GET /notifications/unread?userId=1&role=STUDENT`

**Expected**
- HTTP 200
- only unread rows
- all rows `read = false`

**Status**
✅ Pass

---

## 11) Mark Notification Read

### TC-NOTIF-011

**API**
`PUT /notifications/{id}/read`

**Expected**
- HTTP 200
- `read = true`

**Status**
✅ Pass

---

## 12) Mark Notification Unread

### TC-NOTIF-012

**API**
`PUT /notifications/{id}/unread`

**Expected**
- HTTP 200
- `read = false`

**Status**
✅ Pass

---

## 13) Mark All Read

### TC-NOTIF-013

**API**
`PUT /notifications/mark-all-read?userId=1&role=STUDENT`

**Expected**
- HTTP 200
- all notifications marked read

**Status**
✅ Pass

---

## 14) UI Validation

### TC-NOTIF-014: Parent dashboard notification bell
**Expected**
- notification icon visible
- tap opens notifications page

**Status**
✅ Pass

### TC-NOTIF-015: Student dashboard notification bell
**Expected**
- notification icon visible
- tap opens notifications page

**Status**
✅ Pass

### TC-NOTIF-016: Teacher dashboard notification bell
**Expected**
- notification icon visible
- tap opens notifications page

**Status**
✅ Pass

---

## 15) Regression

### TC-NOTIF-017: Attendance submit still works
**Status**
✅ Pass

### TC-NOTIF-018: Teacher replacements unaffected
**Status**
✅ Pass

### TC-NOTIF-019: Parent dashboard unaffected
**Status**
✅ Pass

### TC-NOTIF-020: Student dashboard unaffected
**Status**
✅ Pass

---

# Final Notification Module Status

| Area | Result |
|---|---|
| Backend APIs | ✅ Pass |
| Broadcast APIs | ✅ Pass |
| Attendance notification triggers | ✅ Pass |
| Read / unread | ✅ Pass |
| UI integration | ✅ Pass |
| Regression | ✅ Pass |

## Module Status
**Production Ready (MVP)**
