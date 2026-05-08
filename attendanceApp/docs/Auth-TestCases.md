# VidyaSetu Authentication Module - Test Cases

## Module Scope
This document validates role-based login and routing for:

- Admin
- Teacher
- Parent
- Student
- Invalid login
- Role-based dashboard routing
- Future security hardening checkpoints

---

## 1) Admin Login

### TC-AUTH-001: Valid admin login
**Expected**
- HTTP 200
- Role = ADMIN
- User navigates to Admin Dashboard
- User/session data stored locally

**Status**
⬜ Pending / Manual

---

## 2) Teacher Login

### TC-AUTH-002: Valid teacher login
**Expected**
- HTTP 200
- Role = TEACHER
- Teacher ID available
- User navigates to Teacher Dashboard

**Status**
⬜ Pending / Manual

---

### TC-AUTH-003: Teacher dashboard uses logged-in teacher ID
**Expected**
- Teacher sees only assigned classes/subjects
- Teacher attendance page loads correct options

**Status**
⬜ Pending / Manual

---

## 3) Parent Login

### TC-AUTH-004: Valid parent login
**Expected**
- HTTP 200
- Role = PARENT
- User navigates to Parent Dashboard
- Parent sees linked student data only

**Status**
⬜ Pending / Manual

---

## 4) Student Login

### TC-AUTH-005: Valid student login
**Expected**
- HTTP 200
- Role = STUDENT
- User navigates to Student Dashboard
- Student sees own data only

**Status**
⬜ Pending / Manual

---

### TC-AUTH-006: Future student first login using admission number + DOB
**Expected**
- Student selects school once
- Login with admission number + DOB
- App forces PIN setup
- Future login uses admission number + PIN

**Status**
⬜ Future

---

## 5) Invalid Login

### TC-AUTH-007: Invalid username
**Expected**
- Login fails
- Error message shown
- No dashboard navigation

**Status**
⬜ Pending / Manual

---

### TC-AUTH-008: Invalid password
**Expected**
- Login fails
- Error message shown
- No local session stored

**Status**
⬜ Pending / Manual

---

### TC-AUTH-009: Empty username/password
**Expected**
- Frontend validation blocks submit
- Required field message shown

**Status**
⬜ Pending / Manual

---

## 6) Role-Based Routing

### TC-AUTH-010: Admin role opens Admin Dashboard
**Status**
⬜ Pending / Manual

### TC-AUTH-011: Teacher role opens Teacher Dashboard
**Status**
⬜ Pending / Manual

### TC-AUTH-012: Parent role opens Parent Dashboard
**Status**
⬜ Pending / Manual

### TC-AUTH-013: Student role opens Student Dashboard
**Status**
⬜ Pending / Manual

---

## 7) Logout

### TC-AUTH-014: Logout clears session
**Expected**
- User returns to login screen
- Stored user data cleared
- Back button should not reopen protected screen

**Status**
⬜ Pending / Manual

---

## 8) Security Regression

### TC-AUTH-015: Direct navigation without login
**Expected**
- Protected pages should redirect to login or block access

**Status**
⬜ Pending / Manual

---

### TC-AUTH-016: Role mismatch access
**Expected**
- Parent cannot access admin dashboard
- Student cannot access teacher attendance page
- Teacher cannot access admin-only functions

**Status**
⬜ Future / Backend RBAC Required

---

## 9) Future Production Security Checklist

### TC-AUTH-017: Password hashing
**Expected**
- Passwords stored using BCrypt
- Plain text passwords not stored

**Status**
⬜ Future

### TC-AUTH-018: JWT token contains role, userId, schoolId
**Status**
⬜ Future

### TC-AUTH-019: Refresh token flow
**Status**
⬜ Future

### TC-AUTH-020: Account lockout after repeated failed attempts
**Status**
⬜ Future

### TC-AUTH-021: Rate limiting on login
**Status**
⬜ Future

---

## Final Status
**Module Status:** MVP Manual Validation Required
