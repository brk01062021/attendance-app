# VidyaSetu MVP - UAT Checklist

## Purpose
This checklist validates VidyaSetu before pilot-school demo or internal release.

---

## 1) Environment

| Item | Expected | Status |
|---|---|---|
| Backend starts | Spring Boot starts without errors | ⬜ |
| Mobile app starts | Expo starts without dependency errors | ⬜ |
| Java/Maven setup | `java -version`, `mvn -version` work | ⬜ |
| Backend API reachable | `localhost:8080` accessible | ⬜ |
| Mobile connects to backend | API calls succeed from app | ⬜ |

---

## 2) Login / Roles

| Role | Expected | Status |
|---|---|---|
| Admin | Opens Admin Dashboard | ⬜ |
| Teacher | Opens Teacher Dashboard | ⬜ |
| Parent | Opens Parent Dashboard | ⬜ |
| Student | Opens Student Dashboard | ⬜ |
| Invalid login | Shows error, no dashboard access | ⬜ |

---

## 3) Teacher Attendance Flow

| Step | Expected | Status |
|---|---|---|
| Teacher dashboard | Loads premium UI | ⬜ |
| Take Attendance card | Opens Load Students page | ⬜ |
| Select subject/class/section | Opens Submit Attendance page | ⬜ |
| Search student | Filters list | ⬜ |
| Clear search | Resets list | ⬜ |
| Bulk present | Updates statuses | ⬜ |
| Summary counts | Present/Late/Absent counts correct | ⬜ |
| Submit attendance | Saves successfully | ⬜ |
| After submit | Returns to Teacher Dashboard | ⬜ |

---

## 4) Parent Module

| Feature | Expected | Status |
|---|---|---|
| Parent dashboard | Loads correctly | ⬜ |
| Attendance details | Opens and displays data | ⬜ |
| Exam results | Opens and displays data | ⬜ |
| Notices | Loads notices | ⬜ |
| Notifications | Opens notification page | ⬜ |
| Reports | Opens reports page | ⬜ |

---

## 5) Student Module

| Feature | Expected | Status |
|---|---|---|
| Student dashboard | Loads correctly | ⬜ |
| Attendance card | Opens attendance details | ⬜ |
| Menu | Opens correctly | ⬜ |
| Exam Results menu | Opens gold page | ⬜ |
| Reports menu | Opens gold page | ⬜ |
| Notifications | Opens notification page | ⬜ |

---

## 6) Admin Module

| Feature | Expected | Status |
|---|---|---|
| Admin dashboard | Loads correctly | ⬜ |
| Whole-school summary | Displays attendance stats | ⬜ |
| Class dashboard | Grouped class data | ⬜ |
| Teacher dashboard | Grouped teacher data | ⬜ |
| Subject dashboard | Grouped subject data | ⬜ |
| Student attendance detail | Filtered records visible | ⬜ |

---

## 7) Notifications

| Feature | Expected | Status |
|---|---|---|
| Single notification | Works | ✅ |
| Bulk notification | Works | ✅ |
| School broadcast | Works | ✅ |
| Class broadcast | Works | ✅ |
| Section broadcast | Works | ✅ |
| Role broadcast | Works | ✅ |
| Weekly attendance notification | Works | ✅ |
| Monthly attendance notification | Works | ✅ |
| Read/unread | Works | ✅ |
| Mark all read | Works | ✅ |

---

## 8) Teacher Replacement

| Feature | Expected | Status |
|---|---|---|
| Teacher Leave Planning | Opens | ⬜ |
| Planned leave | Applies | ⬜ |
| Unplanned leave | Applies | ⬜ |
| Replacement popup | Opens | ⬜ |
| Best Match tab | Shows candidates | ⬜ |
| Same Class tab | Shows candidates | ⬜ |
| Others tab | Shows candidates | ⬜ |
| Assign replacement | Saves replacement | ⬜ |
| Auto assign | Assigns best matches | ⬜ |
| Bulk assign | Works in admin result cards | ⬜ |

---

## 9) Reports

| Feature | Expected | Status |
|---|---|---|
| Attendance report | Works | ⬜ |
| Monthly report | Works | ⬜ |
| Parent report screen | Works | ⬜ |
| Student report screen | Works | ⬜ |
| Admin reports | Work | ⬜ |

---

## 10) UI / Branding

| Item | Expected | Status |
|---|---|---|
| Dark home theme | Preserved | ⬜ |
| Gold internal pages | Preserved | ⬜ |
| Splash backgrounds | No missing backgrounds | ⬜ |
| Watermark/map branding | Visible where expected | ⬜ |
| Cards | Readable and aligned | ⬜ |
| iPhone Expo Go | No layout break | ⬜ |

---

## 11) Regression Signoff

| Area | Status |
|---|---|
| Auth | ⬜ |
| Attendance | ⬜ |
| Parent | ⬜ |
| Student | ⬜ |
| Teacher | ⬜ |
| Admin | ⬜ |
| Notifications | ✅ |
| Teacher Replacement | ⬜ |
| Reports | ⬜ |

---

## 12) Pilot Readiness

| Item | Expected | Status |
|---|---|---|
| Demo school data | Available | ⬜ |
| Dummy users | Available | ⬜ |
| Limited real test phone numbers | Available if needed | ⬜ |
| Import school data plan | Documented | ⬜ |
| Backend stable | No startup errors | ⬜ |
| Mobile stable | No Expo errors | ⬜ |
| Git committed | Latest changes pushed | ⬜ |

---

# Final UAT Result

**Overall Status:** Not Yet Signed Off

**Ready for pilot demo only after all critical MVP rows are marked complete.**
