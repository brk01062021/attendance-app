package com.school.attendance.service;

import com.school.attendance.dto.*;
import com.school.attendance.entity.Attendance;
import com.school.attendance.entity.AttendanceStatus;
import com.school.attendance.entity.Student;
import com.school.attendance.repository.AttendanceRepository;
import com.school.attendance.repository.StudentRepository;
import com.school.attendance.storage.FileStorageService;
import com.school.attendance.storage.StoredFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class MissedAttendanceRecoveryService {
    private static final int RECOVERY_WINDOW_DAYS = 7;
    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final FileStorageService fileStorageService;
    private final Map<String, MissedAttendanceRecoveryResponseDTO> batches = new ConcurrentHashMap<>();
    private final Map<String, List<String>> auditBySchool = new ConcurrentHashMap<>();

    public MissedAttendanceRecoveryService(StudentRepository studentRepository,
                                           AttendanceRepository attendanceRepository,
                                           FileStorageService fileStorageService) {
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
        this.fileStorageService = fileStorageService;
    }

    public byte[] template() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Missed Attendance");
            Row header = sheet.createRow(0);
            String[] headers = {"Student ID", "Class", "Section", "Attendance Date", "Status", "Reason"};
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);
            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("ST1024");
            sample.createCell(1).setCellValue("10");
            sample.createCell(2).setCellValue("A");
            sample.createCell(3).setCellValue(LocalDate.now().minusDays(1).toString());
            sample.createCell(4).setCellValue("PRESENT");
            sample.createCell(5).setCellValue("Recovered missed entry after Admin review");
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create missed attendance template.");
        }
    }

    public MissedAttendanceRecoveryResponseDTO validate(MultipartFile file, String schoolId, String uploadedBy) {
        String safeSchoolId = safeSchoolId(schoolId);
        List<MissedAttendanceRecoveryIssueDTO> issues = new ArrayList<>();
        List<MissedAttendanceRecoveryRowDTO> rows = new ArrayList<>();
        if (file == null || file.isEmpty()) {
            issues.add(new MissedAttendanceRecoveryIssueDTO(0, "FILE", "ERROR", "Upload the missed attendance Excel file."));
            return build(null, safeSchoolId, rows, issues, "VALIDATION_FAILED", "Upload a valid missed attendance workbook.");
        }
        Map<String, Student> studentIndex = studentIndex();
        try {
            byte[] bytes = file.getBytes();
            StoredFile storedFile = fileStorageService.uploadAttendanceRecovery(safeSchoolId, file, bytes);
            try (InputStream in = new ByteArrayInputStream(bytes); Workbook workbook = new XSSFWorkbook(in)) {
                Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
                if (sheet == null) {
                    issues.add(new MissedAttendanceRecoveryIssueDTO(0, "FILE", "ERROR", "Workbook must contain a missed attendance sheet."));
                    return build(null, safeSchoolId, rows, issues, "VALIDATION_FAILED", "Upload a valid missed attendance workbook.");
                }
                DataFormatter formatter = new DataFormatter();
                Map<String,Integer> headers = headers(sheet.getRow(0), formatter);
                for (String required : List.of("student id", "class", "section", "attendance date", "status", "reason")) {
                    if (!headers.containsKey(required)) issues.add(new MissedAttendanceRecoveryIssueDTO(1, "MISSING_COLUMN", "ERROR", "Missing required column: " + title(required)));
                }
                if (!issues.isEmpty()) return build(null, safeSchoolId, rows, issues, "VALIDATION_FAILED", "Required columns are missing.");
                Set<String> fileDuplicates = new HashSet<>();
                for (int i=1; i<=sheet.getLastRowNum(); i++) {
                    Row r = sheet.getRow(i); if (r == null) continue;
                    MissedAttendanceRecoveryRowDTO dto = new MissedAttendanceRecoveryRowDTO();
                    dto.setRowNumber(i+1);
                    dto.setStudentId(cell(r, headers.get("student id"), formatter));
                    dto.setClassName(cell(r, headers.get("class"), formatter));
                    dto.setSection(cell(r, headers.get("section"), formatter));
                    dto.setAttendanceDate(cell(r, headers.get("attendance date"), formatter));
                    dto.setStatus(cell(r, headers.get("status"), formatter).toUpperCase(Locale.ROOT));
                    dto.setReason(cell(r, headers.get("reason"), formatter));
                    if (isBlank(dto.getStudentId()) && isBlank(dto.getClassName()) && isBlank(dto.getSection()) && isBlank(dto.getAttendanceDate()) && isBlank(dto.getStatus())) continue;
                    validateRow(dto, issues, studentIndex, fileDuplicates);
                    dto.setValid(issues.stream().noneMatch(x -> x.getRowNumber() == dto.getRowNumber() && "ERROR".equalsIgnoreCase(x.getSeverity())));
                    rows.add(dto);
                }
            }
        } catch (Exception ex) {
            issues.add(new MissedAttendanceRecoveryIssueDTO(0, "FILE", "ERROR", "Unable to read the Excel file. Upload a valid .xlsx workbook."));
        }
        String batchId = "ATT-REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        MissedAttendanceRecoveryResponseDTO response = build(batchId, safeSchoolId, rows, issues, issues.stream().anyMatch(i -> "ERROR".equalsIgnoreCase(i.getSeverity())) ? "VALIDATION_FAILED" : "VALIDATED", issues.stream().anyMatch(i -> "ERROR".equalsIgnoreCase(i.getSeverity())) ? "Resolve validation issues before submit." : "Validation completed. Ready for submit.");
        batches.put(batchId, response);
        audit(safeSchoolId, "Validated missed attendance recovery batch " + batchId + " by " + safe(uploadedBy, "Admin") + ".");
        return response;
    }

    public MissedAttendanceRecoveryResponseDTO submit(String batchId, String schoolId, String submittedBy) {
        MissedAttendanceRecoveryResponseDTO response = batches.get(batchId);
        if (response == null) {
            MissedAttendanceRecoveryResponseDTO missing = new MissedAttendanceRecoveryResponseDTO();
            missing.setRecoveryBatchId(batchId); missing.setSchoolId(safeSchoolId(schoolId)); missing.setStatus("NOT_FOUND");
            missing.setMessage("Recovery batch was not found. Validate the workbook again.");
            return missing;
        }
        if (!response.isCanSubmit()) { response.setStatus("SUBMIT_BLOCKED"); response.setMessage("Resolve validation issues before submit."); return response; }
        Map<String, Student> studentIndex = studentIndex();
        int submitted = 0;
        for (MissedAttendanceRecoveryRowDTO row : response.getRows()) {
            if (!row.isValid()) continue;
            Student student = studentIndex.get(norm(row.getStudentId()));
            LocalDate date = parseDate(row.getAttendanceDate());
            if (student == null || date == null) continue;
            Attendance attendance = new Attendance();
            attendance.setStudent(student);
            attendance.setAttendanceDate(date);
            attendance.setClassName(row.getClassName());
            attendance.setSection(row.getSection());
            attendance.setStatus(AttendanceStatus.valueOf(row.getStatus().toUpperCase(Locale.ROOT)));
            attendance.setTeacherId(0L);
            attendance.setTeacherName("Attendance Recovery");
            attendance.setSubjectName("RECOVERY");
            attendanceRepository.save(attendance);
            submitted++;
        }
        response.setSubmittedRows(submitted);
        response.setStatus("SUBMITTED");
        response.setCanSubmit(false);
        response.setMessage("Recovery submitted. Attendance history updated.");
        audit(response.getSchoolId(), "Submitted missed attendance recovery batch " + batchId + " by " + safe(submittedBy, "Admin") + ". Rows updated: " + submitted + ".");
        return response;
    }

    public MissedAttendanceRecoveryStatusDTO status(String schoolId) {
        String safeSchoolId = safeSchoolId(schoolId);
        MissedAttendanceRecoveryStatusDTO dto = new MissedAttendanceRecoveryStatusDTO();
        dto.setSchoolId(safeSchoolId);
        List<String> audits = auditBySchool.getOrDefault(safeSchoolId, List.of());
        dto.setAuditTrail(audits);
        Optional<MissedAttendanceRecoveryResponseDTO> latest = batches.values().stream().filter(b -> safeSchoolId.equalsIgnoreCase(b.getSchoolId())).reduce((a,b)->b);
        if (latest.isEmpty()) {
            dto.setStatus("NO_RECOVERY"); dto.setLabel("No Recovery Submitted"); dto.setMessage("Use Web ERP to recover missed attendance when required."); return dto;
        }
        dto.setLatestRecoveryBatchId(latest.get().getRecoveryBatchId());
        dto.setSubmittedRows(latest.get().getSubmittedRows());
        dto.setStatus(latest.get().getStatus());
        dto.setLabel("Recovery " + title(latest.get().getStatus().toLowerCase(Locale.ROOT)));
        dto.setMessage(latest.get().getMessage());
        return dto;
    }

    private void validateRow(MissedAttendanceRecoveryRowDTO dto, List<MissedAttendanceRecoveryIssueDTO> issues, Map<String, Student> students, Set<String> fileDuplicates) {
        if (isBlank(dto.getStudentId())) issues.add(issue(dto, "Missing Student ID", "Student ID is required."));
        Student student = isBlank(dto.getStudentId()) ? null : students.get(norm(dto.getStudentId()));
        if (!isBlank(dto.getStudentId()) && student == null) issues.add(issue(dto, "Unknown Student", "Student ID was not found in this workspace."));
        if (isBlank(dto.getClassName()) || isBlank(dto.getSection())) issues.add(issue(dto, "Missing Class/Section", "Class and Section are required."));
        if (student != null) {
            dto.setStudentName(student.getName());
            if (!isBlank(dto.getClassName()) && !student.getClassName().equalsIgnoreCase(dto.getClassName())) issues.add(issue(dto, "Missing Class/Section", "Class does not match the student record."));
            if (!isBlank(dto.getSection()) && !student.getSection().equalsIgnoreCase(dto.getSection())) issues.add(issue(dto, "Missing Class/Section", "Section does not match the student record."));
        }
        LocalDate date = parseDate(dto.getAttendanceDate());
        if (date == null) issues.add(issue(dto, "Invalid Date", "Use date format YYYY-MM-DD."));
        else {
            if (date.isAfter(LocalDate.now())) issues.add(issue(dto, "Future Date", "Attendance recovery cannot be submitted for a future date."));
            if (date.isBefore(LocalDate.now().minusDays(RECOVERY_WINDOW_DAYS))) issues.add(issue(dto, "Date outside allowed recovery window", "Recover attendance within the last 7 days."));
        }
        if (!Set.of("PRESENT", "ABSENT", "LATE").contains(String.valueOf(dto.getStatus()).toUpperCase(Locale.ROOT))) issues.add(issue(dto, "Invalid Status", "Use PRESENT, ABSENT, or LATE."));
        String key = norm(dto.getStudentId()) + "|" + dto.getAttendanceDate();
        if (!fileDuplicates.add(key)) issues.add(issue(dto, "Duplicate Attendance Recovery", "This student/date appears more than once in the recovery file."));
        if (student != null && date != null && attendanceRepository.findByStudentIdAndAttendanceDateBetween(student.getId(), date, date).stream().anyMatch(a -> eq(a.getClassName(), dto.getClassName()) && eq(a.getSection(), dto.getSection()))) {
            issues.add(issue(dto, "Duplicate Attendance Recovery", "Attendance already exists for this student and date."));
        }
    }

    private MissedAttendanceRecoveryIssueDTO issue(MissedAttendanceRecoveryRowDTO row, String category, String message) { return new MissedAttendanceRecoveryIssueDTO(row.getRowNumber(), category, "ERROR", message); }
    private MissedAttendanceRecoveryResponseDTO build(String batchId, String schoolId, List<MissedAttendanceRecoveryRowDTO> rows, List<MissedAttendanceRecoveryIssueDTO> issues, String status, String message) {
        MissedAttendanceRecoveryResponseDTO r = new MissedAttendanceRecoveryResponseDTO();
        r.setRecoveryBatchId(batchId); r.setSchoolId(schoolId); r.setRows(rows); r.setIssues(issues); r.setStatus(status); r.setMessage(message);
        int errors = (int) issues.stream().filter(i -> "ERROR".equalsIgnoreCase(i.getSeverity())).count();
        int warnings = (int) issues.stream().filter(i -> "WARNING".equalsIgnoreCase(i.getSeverity())).count();
        r.setErrorCount(errors); r.setWarningCount(warnings); r.setTotalRows(rows.size()); r.setAcceptedRows((int) rows.stream().filter(MissedAttendanceRecoveryRowDTO::isValid).count());
        r.setValid(errors == 0); r.setCanSubmit(batchId != null && errors == 0 && !rows.isEmpty());
        Map<String, Long> grouped = issues.stream().collect(Collectors.groupingBy(MissedAttendanceRecoveryIssueDTO::getCategory, LinkedHashMap::new, Collectors.counting()));
        r.setValidationCards(grouped.entrySet().stream().map(e -> new MissedAttendanceRecoveryCardDTO(e.getKey(), e.getValue().intValue(), "ERROR", guidance(e.getKey()))).toList());
        return r;
    }
    private String guidance(String category) { return switch (category) { case "Missing Student ID" -> "Enter Student ID for every recovery row."; case "Unknown Student" -> "Use a Student ID available in the active workspace."; case "Missing Class/Section" -> "Class and Section must match the student record."; case "Invalid Date" -> "Use YYYY-MM-DD."; case "Future Date" -> "Use today or a previous date only."; case "Duplicate Attendance Recovery" -> "Remove duplicate rows or existing attendance entries."; case "Invalid Status" -> "Use PRESENT, ABSENT, or LATE."; case "Date outside allowed recovery window" -> "Recover attendance within the last 7 days."; default -> "Review the recovery workbook and upload again."; }; }
    private Map<String,Integer> headers(Row row, DataFormatter f) { Map<String,Integer> m=new HashMap<>(); if(row==null)return m; for(Cell c: row) m.put(f.formatCellValue(c).trim().toLowerCase(Locale.ROOT), c.getColumnIndex()); return m; }
    private String cell(Row r, Integer idx, DataFormatter f) { return idx == null ? "" : f.formatCellValue(r.getCell(idx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)).trim(); }
    private LocalDate parseDate(String v) { try { return LocalDate.parse(v, DateTimeFormatter.ISO_LOCAL_DATE); } catch (DateTimeParseException ex) { return null; } }
    private Map<String, Student> studentIndex() {
        Map<String, Student> map = new HashMap<>();
        for (Student s : studentRepository.findAll()) {
            if (s.getId() != null) map.put(norm(String.valueOf(s.getId())), s);
            if (!isBlank(s.getAdmissionNumber())) map.put(norm(s.getAdmissionNumber()), s);
            if (!isBlank(s.getRollNumber())) map.put(norm(s.getRollNumber()), s);
        }
        return map;
    }
    private String norm(String v) { return String.valueOf(v == null ? "" : v).trim().toUpperCase(Locale.ROOT); }
    private boolean eq(String a, String b) { return norm(a).equals(norm(b)); }
    private boolean isBlank(String v) { return v == null || v.trim().isEmpty(); }
    private String safe(String v, String d) { return isBlank(v) ? d : v.trim(); }
    private String safeSchoolId(String s) { return isBlank(s) ? "DEMO" : s.trim().toUpperCase(Locale.ROOT); }
    private String title(String s) { return Arrays.stream(s.replace('_',' ').split(" ")).filter(x->!x.isBlank()).map(x -> x.substring(0,1).toUpperCase()+x.substring(1).toLowerCase()).collect(Collectors.joining(" ")); }
    private void audit(String schoolId, String msg) { auditBySchool.computeIfAbsent(schoolId, k -> new ArrayList<>()).add(0, LocalDate.now() + " • " + msg); }
}
