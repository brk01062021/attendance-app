# Teacher Operations Test Cases

## Module 1: Teacher Leave Planning

### Functional Test Cases

| ID       | Test Case                         | Steps                                          | Expected Result                                                     |
|----------|-----------------------------------|------------------------------------------------|---------------------------------------------------------------------|
| TLP-F-01 | Load teacher list                 | Open Teacher Leave Planning screen             | Teacher list should load                                            |
| TLP-F-02 | Select leave type Planned Leave   | Click Planned Leave                            | Planned Leave should be selected                                    |
| TLP-F-03 | Select leave type Unplanned Leave | Click Unplanned Leave                          | Unplanned Leave should be selected and duration should stay one day |
| TLP-F-04 | Select one-day leave              | Select One Day and date                        | Only one date should be used                                        |
| TLP-F-05 | Select multi-day leave            | Select Multiple Days, From Date, To Date       | Schedules should load across date range                             |
| TLP-F-06 | Load teacher schedule             | Select teacher and click Load Teacher Schedule | Period cards should display                                         |
| TLP-F-07 | Apply planned leave               | Click Apply Planned Leave                      | Visible periods should update to PLANNED_LEAVE                      |
| TLP-F-08 | Apply unplanned leave             | Click Apply Unplanned Leave                    | Visible periods should update to UNPLANNED_LEAVE                    |
| TLP-F-09 | Open replacement options          | Click Replacement Options                      | Replacement popup should open                                       |
| TLP-F-10 | Assign replacement teacher        | Select replacement teacher and save            | Replacement teacher should be assigned                              |
| TLP-F-11 | Select date group for bulk assign | Click date group header                        | Bulk assign confirm popup should appear                             |
| TLP-F-12 | Confirm bulk assign selection     | Click Yes/OK in popup                          | Pending periods should be selected                                  |
| TLP-F-13 | Auto assign best matches          | Click Auto Assign Best Matches                 | Best available replacements should be assigned                      |
| TLP-F-14 | Reset after success               | Click OK on success popup                      | Screen should refresh/reset correctly                               |

---

### API Test Cases

| ID       | API                                                                    | Method | Expected Result                      |
|----------|------------------------------------------------------------------------|--------|--------------------------------------|
| TLP-A-01 | `/teacher-schedules/teachers`                                          | GET    | Returns teacher list                 |
| TLP-A-02 | `/teacher-schedules?date=YYYY-MM-DD`                                   | GET    | Returns schedules for selected date  |
| TLP-A-03 | `/teacher-schedules/{id}/status?status=PLANNED_LEAVE`                  | PUT    | Updates schedule to planned leave    |
| TLP-A-04 | `/teacher-schedules/{id}/status?status=UNPLANNED_LEAVE`                | PUT    | Updates schedule to unplanned leave  |
| TLP-A-05 | `/teacher-schedules/available-replacements?scheduleId={id}`            | GET    | Returns bestMatch, sameClass, others |
| TLP-A-06 | `/teacher-schedules/{id}/assign-replacement?replacementTeacherId={id}` | PUT    | Assigns replacement                  |
| TLP-A-07 | `/teacher-schedules/bulk-assign-replacement`                           | PUT    | Bulk assigns replacement             |
| TLP-A-08 | `/teacher-schedules/auto-assign-best-matches?date=YYYY-MM-DD`          | POST   | Auto assigns best matches            |

---

### UI Test Cases

| ID       | Test Case                     | Expected Result                                          |
|----------|-------------------------------|----------------------------------------------------------|
| TLP-U-01 | Gold premium buttons          | Buttons should match app theme                           |
| TLP-U-02 | Date calendar modal           | Calendar should open and select date                     |
| TLP-U-03 | Summary card                  | Shows total periods, batch count, assigned, not assigned |
| TLP-U-04 | Date group header             | Header should be clickable                               |
| TLP-U-05 | Bulk popup                    | Popup should show Yes/Cancel                             |
| TLP-U-06 | Auto assign button visibility | Button appears only after selected pending periods       |
| TLP-U-07 | Replacement popup tabs        | Best Match, Same Class, Others should work               |
| TLP-U-08 | Success popup                 | Success popup should show after save/apply               |
| TLP-U-09 | Reset state                   | After OK, screen should clear correctly                  |

---

### Edge Case Test Cases

| ID       | Test Case                      | Expected Result                |
|----------|--------------------------------|--------------------------------|
| TLP-E-01 | No schedules found             | Show No Schedule Found         |
| TLP-E-02 | From date after To date        | Show invalid date range alert  |
| TLP-E-03 | No replacement teachers        | Show No Teachers message       |
| TLP-E-04 | Save without selecting teacher | Show validation alert          |
| TLP-E-05 | Bulk assign without selection  | Show validation alert          |
| TLP-E-06 | API failure                    | Show error alert               |
| TLP-E-07 | Network failure                | Show unable to load/save alert |
| TLP-E-08 | More than 10 periods           | Batch display should work      |

---

### Role/Security Test Cases

| ID       | Test Case             | Expected Result                                               |
|----------|-----------------------|---------------------------------------------------------------|
| TLP-S-01 | Admin access          | Admin can access Teacher Leave Planning                       |
| TLP-S-02 | Teacher access        | Teacher should not access admin leave planning unless allowed |
| TLP-S-03 | Parent access         | Parent should not access this screen/API                      |
| TLP-S-04 | Student access        | Student should not access this screen/API                     |
| TLP-S-05 | Unauthorized API call | Should return 401/403                                         |
| TLP-S-06 | Invalid schedule ID   | Should not update records                                     |

---

## Module 2: Admin Teacher Dashboard

### Functional Test Cases

| ID       | Test Case                       | Steps                                     | Expected Result                            |
|----------|---------------------------------|-------------------------------------------|--------------------------------------------|
| ATD-F-01 | Load filter options             | Select date and click Load Filter Options | Teacher/class/section/subject filters load |
| ATD-F-02 | Filter by teacher               | Select teacher chip                       | Related schedules should filter            |
| ATD-F-03 | Filter by class                 | Select class chip                         | Related schedules should filter            |
| ATD-F-04 | Filter by section               | Select section chip                       | Related schedules should filter            |
| ATD-F-05 | Filter by subject               | Select subject chip                       | Related schedules should filter            |
| ATD-F-06 | Filter by status                | Select status chip                        | Related schedules should filter            |
| ATD-F-07 | Filter by replacement           | Select ASSIGNED/NOT_ASSIGNED              | Replacement filtered results should show   |
| ATD-F-08 | Load teacher schedule           | Click Load Teacher Schedule               | Result cards should show                   |
| ATD-F-09 | Mark unplanned leave            | Click Mark Unplanned Leave                | Replacement popup opens and status updates |
| ATD-F-10 | Assign replacement              | Click Assign Replacement                  | Replacement popup opens                    |
| ATD-F-11 | Save replacement                | Select teacher and save                   | Schedule updated successfully              |
| ATD-F-12 | Reset after success             | Click OK on success popup                 | Screen returns to default state            |
| ATD-F-13 | Available card replacement info | View AVAILABLE card                       | Replacement info should not show           |
| ATD-F-14 | Leave card replacement info     | View PLANNED/UNPLANNED card               | Replacement info should show               |

---

### API Test Cases

| ID       | API                                                                    | Method | Expected Result                     |
|----------|------------------------------------------------------------------------|--------|-------------------------------------|
| ATD-A-01 | `/teacher-schedules?date=YYYY-MM-DD`                                   | GET    | Returns all schedules               |
| ATD-A-02 | `/teacher-schedules/{id}/status?status=UNPLANNED_LEAVE`                | PUT    | Updates to unplanned leave          |
| ATD-A-03 | `/teacher-schedules/{id}/status?status=AVAILABLE`                      | PUT    | Updates to available                |
| ATD-A-04 | `/teacher-schedules/available-replacements?scheduleId={id}`            | GET    | Returns grouped replacement options |
| ATD-A-05 | `/teacher-schedules/{id}/assign-replacement?replacementTeacherId={id}` | PUT    | Assigns replacement                 |
| ATD-A-06 | `/teacher-schedules/auto-assign-best-matches?date=YYYY-MM-DD`          | POST   | Auto assigns pending replacements   |

---

### UI Test Cases

| ID       | Test Case         | Expected Result                                          |
|----------|-------------------|----------------------------------------------------------|
| ATD-U-01 | Dashboard title   | Shows Admin Teacher's Dashboard                          |
| ATD-U-02 | Schedule date box | Date selector should work                                |
| ATD-U-03 | Filter chips      | Chips should scroll horizontally                         |
| ATD-U-04 | Result summary    | Shows showing count and reset filters                    |
| ATD-U-05 | Result cards      | Cards should show teacher, class, section, subject, time |
| ATD-U-06 | Available card    | Should show status and action buttons only               |
| ATD-U-07 | Leave card        | Should show replacement info                             |
| ATD-U-08 | Success popup     | Should show after save                                   |
| ATD-U-09 | Default redirect  | After OK, only Load Filter Options screen should show    |

---

### Edge Case Test Cases

| ID       | Test Case                | Expected Result                         |
|----------|--------------------------|-----------------------------------------|
| ATD-E-01 | No schedules found       | Show No Schedule Found                  |
| ATD-E-02 | No records after filters | Show No Records Found                   |
| ATD-E-03 | Replacement list empty   | Show No Teachers                        |
| ATD-E-04 | Save without selection   | Show select teacher alert               |
| ATD-E-05 | API failure              | Show unable to save alert               |
| ATD-E-06 | Reset filters            | Filters reset to ALL                    |
| ATD-E-07 | Duplicate teachers       | Teacher filter should show unique names |
| ATD-E-08 | Available card           | Replacement info should be hidden       |

---

### Role/Security Test Cases

| ID       | Test Case                   | Expected Result                           |
|----------|-----------------------------|-------------------------------------------|
| ATD-S-01 | Admin access                | Admin can access dashboard                |
| ATD-S-02 | Teacher access              | Teacher should not access admin dashboard |
| ATD-S-03 | Parent access               | Parent should not access dashboard/API    |
| ATD-S-04 | Student access              | Student should not access dashboard/API   |
| ATD-S-05 | Unauthorized request        | API should return 401/403                 |
| ATD-S-06 | Invalid replacement teacher | API should reject invalid teacher         |

---

## Regression Test Cases

| ID     | Test Case                     | Expected Result                                          |
|--------|-------------------------------|----------------------------------------------------------|
| REG-01 | Teacher dashboard still loads | Existing teacher dashboard should work                   |
| REG-02 | Attendance submit still works | Attendance submission should not break                   |
| REG-03 | Admin dashboard still loads   | Admin attendance dashboard should work                   |
| REG-04 | Login role flow               | Admin/Teacher/Parent/Student login should remain correct |
| REG-05 | API endpoint compatibility    | Existing endpoints should not break                      |
| REG-06 | Calendar modal reuse          | Calendar should work in all screens                      |
| REG-07 | Gold UI theme                 | Theme should stay consistent                             |
| REG-08 | Navigation                    | Back/Home navigation should work                         |
| REG-09 | Replacement assignment        | Single replacement should still work after bulk changes  |
| REG-10 | Auto assign                   | Auto assign should not affect already assigned records   |

---

## Final Validation Checklist

- [ ] Teacher Leave Planning loads
- [ ] Admin Teacher Dashboard loads
- [ ] Planned leave works
- [ ] Unplanned leave works
- [ ] Replacement assignment works
- [ ] Bulk assign popup works
- [ ] Auto assign best matches works
- [ ] AVAILABLE cards hide replacement info
- [ ] Leave cards show replacement info
- [ ] Success OK resets screen to default
- [ ] Role/security rules verified
- [ ] No regression in attendance flow