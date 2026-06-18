package com.school.attendance.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.attendance.dto.FeeReminderDtos;
import com.school.attendance.entity.AppUser;
import com.school.attendance.entity.Notification;
import com.school.attendance.entity.SchoolImportStagingRecord;
import com.school.attendance.entity.Student;
import com.school.attendance.entity.FeeReminderHistory;
import com.school.attendance.entity.FeeReminderRow;
import com.school.attendance.entity.FeeReminderUpload;
import com.school.attendance.repository.AppUserRepository;
import com.school.attendance.repository.NotificationRepository;
import com.school.attendance.repository.SchoolImportStagingRecordRepository;
import com.school.attendance.repository.StudentRepository;
import com.school.attendance.repository.FeeReminderHistoryRepository;
import com.school.attendance.repository.FeeReminderRowRepository;
import com.school.attendance.repository.FeeReminderUploadRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FeeReminderService {
    private static final String READY = "READY";
    private static final String INVALID_ROW = "INVALID_ROW";
    private static final String MISSING_STUDENT = "MISSING_STUDENT";
    private static final String MISSING_PARENT_MAPPING = "MISSING_PARENT_MAPPING";
    private static final String SENT = "SENT";
    private final FeeReminderUploadRepository uploadRepository;
    private final FeeReminderRowRepository rowRepository;
    private final FeeReminderHistoryRepository historyRepository;
    private final StudentRepository studentRepository;
    private final AppUserRepository appUserRepository;
    private final NotificationRepository notificationRepository;
    private final SchoolImportStagingRecordRepository stagingRecordRepository;
    private final ObjectMapper objectMapper;

    public FeeReminderService(FeeReminderUploadRepository uploadRepository, FeeReminderRowRepository rowRepository, FeeReminderHistoryRepository historyRepository, StudentRepository studentRepository, AppUserRepository appUserRepository, NotificationRepository notificationRepository, SchoolImportStagingRecordRepository stagingRecordRepository, ObjectMapper objectMapper) {
        this.uploadRepository = uploadRepository;
        this.rowRepository = rowRepository;
        this.historyRepository = historyRepository;
        this.studentRepository = studentRepository;
        this.appUserRepository = appUserRepository;
        this.notificationRepository = notificationRepository;
        this.stagingRecordRepository = stagingRecordRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public FeeReminderDtos.Preview upload(String schoolId, MultipartFile file, String uploadedBy) {
        String safeSchoolId = normalizeSchoolId(schoolId);
        FeeReminderUpload upload = new FeeReminderUpload();
        upload.setSchoolId(safeSchoolId);
        upload.setOriginalFilename(file == null ? "fee-reminders.xlsx" : file.getOriginalFilename());
        upload.setUploadedBy(uploadedBy);
        upload = uploadRepository.save(upload);

        List<FeeReminderRow> rows = parseRows(upload.getId(), safeSchoolId, file);
        rows.forEach(this::validateAndMap);
        rowRepository.saveAll(rows);
        refreshCounts(upload, rows);
        return preview(upload.getId(), safeSchoolId);
    }

    public FeeReminderDtos.Preview preview(Long uploadId, String schoolId) {
        FeeReminderUpload upload = loadUpload(uploadId, schoolId);
        List<FeeReminderDtos.Row> rows = rowRepository.findByUploadIdOrderByRowNumberAsc(uploadId).stream().map(this::toRow).toList();
        return new FeeReminderDtos.Preview(toSummary(upload), rows);
    }

    public FeeReminderDtos.Summary summary(Long uploadId, String schoolId) {
        return toSummary(loadUpload(uploadId, schoolId));
    }

    public List<FeeReminderDtos.Summary> uploads(String schoolId) {
        return uploadRepository.findTop20BySchoolIdIgnoreCaseOrderByCreatedAtDesc(normalizeSchoolId(schoolId)).stream().map(this::toSummary).toList();
    }

    @Transactional
    public FeeReminderDtos.SendResult send(Long uploadId, String schoolId, String sentBy) {
        FeeReminderUpload upload = loadUpload(uploadId, schoolId);
        List<FeeReminderRow> readyRows = rowRepository.findByUploadIdAndStatusOrderByRowNumberAsc(uploadId, READY);
        int notificationsCreated = 0;
        int rowsSent = 0;
        for (FeeReminderRow row : readyRows) {
            List<AppUser> parents = parentUsers(row);
            if (parents.isEmpty()) {
                row.setStatus(MISSING_PARENT_MAPPING);
                row.setValidationMessage("No mapped parent account found for this student.");
                continue;
            }
            for (AppUser parent : parents) {
                notificationRepository.save(buildNotification(parent, row));
                historyRepository.save(buildHistory(upload, row, parent));
                notificationsCreated++;
            }
            row.setStatus(SENT);
            row.setValidationMessage("Reminder sent to mapped parent account(s).");
            rowsSent++;
        }
        rowRepository.saveAll(readyRows);
        List<FeeReminderRow> allRows = rowRepository.findByUploadIdOrderByRowNumberAsc(uploadId);
        upload.setSentAt(LocalDateTime.now());
        refreshCounts(upload, allRows);
        upload.setStatus(upload.getReadyRows() == 0 ? "SENT" : "PARTIALLY_SENT");
        uploadRepository.save(upload);
        return new FeeReminderDtos.SendResult(toSummary(upload), notificationsCreated, rowsSent, Math.max(0, allRows.size() - rowsSent));
    }

    public List<FeeReminderDtos.History> history(String schoolId) {
        return historyRepository.findTop100BySchoolIdIgnoreCaseOrderBySentAtDesc(normalizeSchoolId(schoolId)).stream().map(this::toHistory).toList();
    }

    public List<FeeReminderDtos.History> parentHistory(String schoolId, Long parentUserId) {
        return historyRepository.findTop50BySchoolIdIgnoreCaseAndParentUserIdOrderBySentAtDesc(normalizeSchoolId(schoolId), parentUserId).stream().map(this::toHistory).toList();
    }

    private List<FeeReminderRow> parseRows(Long uploadId, String schoolId, MultipartFile file) {
        List<FeeReminderRow> rows = new ArrayList<>();
        try (InputStream input = file.getInputStream(); Workbook workbook = new XSSFWorkbook(input)) {
            Sheet sheet = workbook.getNumberOfSheets() == 0 ? null : workbook.getSheetAt(0);
            if (sheet == null) return rows;
            Row header = sheet.getRow(sheet.getFirstRowNum());
            Map<String, Integer> columns = headerMap(header);
            for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row excelRow = sheet.getRow(i);
                if (excelRow == null || isBlankRow(excelRow)) continue;
                FeeReminderRow row = new FeeReminderRow();
                row.setUploadId(uploadId);
                row.setSchoolId(schoolId);
                row.setRowNumber(i + 1);
                row.setStudentId(text(excelRow, columns, "student_id", "student id", "admission_no", "admission number"));
                row.setStudentName(text(excelRow, columns, "student_name", "student name", "name"));
                row.setClassName(text(excelRow, columns, "class", "class_name", "class name"));
                row.setSection(text(excelRow, columns, "section"));
                row.setPendingAmount(amount(text(excelRow, columns, "pending_amount", "pending amount", "amount", "fee_due")));
                row.setDueDate(date(excelRow, columns, "due_date", "due date"));
                row.setRemarks(text(excelRow, columns, "remarks", "note", "comments"));
                rows.add(row);
            }
        } catch (Exception e) {
            FeeReminderRow row = new FeeReminderRow();
            row.setUploadId(uploadId); row.setSchoolId(schoolId); row.setRowNumber(1); row.setStatus(INVALID_ROW);
            row.setValidationMessage("Unable to read workbook. Upload a valid .xlsx file.");
            rows.add(row);
        }
        return rows;
    }

    private void validateAndMap(FeeReminderRow row) {
        List<String> errors = new ArrayList<>();
        if (blank(row.getStudentId()) && blank(row.getStudentName())) errors.add("Student ID or Student Name is required.");
        if (row.getPendingAmount() == null || row.getPendingAmount().compareTo(BigDecimal.ZERO) <= 0) errors.add("Pending Amount must be greater than zero.");
        if (row.getDueDate() == null) errors.add("Due Date is required.");
        if (!errors.isEmpty()) { row.setStatus(INVALID_ROW); row.setValidationMessage(String.join(" ", errors)); return; }
        Optional<Student> student = findStudent(row);
        if (student.isEmpty()) { row.setStatus(MISSING_STUDENT); row.setValidationMessage("Student could not be matched by Student ID or Student Name + Class."); return; }
        row.setMappedStudentDbId(student.get().getId());
        if (blank(row.getStudentName())) row.setStudentName(student.get().getName());
        if (blank(row.getClassName())) row.setClassName(student.get().getClassName());
        if (blank(row.getSection())) row.setSection(student.get().getSection());
        List<AppUser> parents = parentUsers(row);
        if (parents.isEmpty()) { row.setStatus(MISSING_PARENT_MAPPING); row.setValidationMessage("Student matched, but no parent account mapping was found."); return; }
        row.setMappedParentUserIds(parents.stream().map(p -> String.valueOf(p.getId())).collect(Collectors.joining(",")));
        row.setMappedParentNames(parents.stream().map(AppUser::getDisplayName).collect(Collectors.joining(", ")));
        row.setStatus(READY); row.setValidationMessage("Ready to send.");
    }

    private Optional<Student> findStudent(FeeReminderRow row) {
        List<Student> all = studentRepository.findAllByOrderByClassNameAscSectionAscNameAsc();
        String sid = norm(row.getStudentId());
        if (!sid.isBlank()) {
            Optional<Student> byAdmission = all.stream().filter(s -> sid.equals(norm(s.getAdmissionNumber())) || sid.equals(norm(s.getRollNumber()))).findFirst();
            if (byAdmission.isPresent()) return byAdmission;
        }
        String name = norm(row.getStudentName()); String cls = norm(row.getClassName());
        if (!name.isBlank()) {
            return all.stream().filter(s -> name.equals(norm(s.getName())) && (cls.isBlank() || cls.equals(norm(s.getClassName())))).findFirst();
        }
        return Optional.empty();
    }

    private List<AppUser> parentUsers(FeeReminderRow row) {
        if (!blank(row.getMappedParentUserIds())) {
            return Arrays.stream(row.getMappedParentUserIds().split(",")).map(String::trim).filter(s -> !s.isBlank()).map(Long::valueOf).map(appUserRepository::findById).flatMap(Optional::stream).toList();
        }
        Set<String> mobiles = parentMobilesForStudent(row.getSchoolId(), row.getStudentId());
        List<AppUser> parents = appUserRepository.findByRoleIgnoreCaseAndSchoolCodeIgnoreCase("PARENT", row.getSchoolId());
        if (!mobiles.isEmpty()) {
            List<AppUser> matched = parents.stream().filter(p -> mobiles.contains(digits(p.getUsername()))).toList();
            if (!matched.isEmpty()) return matched;
        }
        String studentRef = norm(row.getStudentId());
        return parents.stream().filter(p -> !studentRef.isBlank() && norm(p.getTeacherName()).equals(studentRef)).toList();
    }

    private Set<String> parentMobilesForStudent(String schoolId, String studentId) {
        if (blank(studentId)) return Set.of();
        String target = norm(studentId);
        return stagingRecordRepository.findAll().stream()
                .filter(r -> schoolId.equalsIgnoreCase(r.getSchoolCode()) && "Parents".equalsIgnoreCase(r.getSheetName()))
                .map(SchoolImportStagingRecord::getRowJson).map(this::jsonMap)
                .filter(m -> target.equals(norm(first(m, "admission_no", "student_id", "studentid"))))
                .map(m -> digits(first(m, "mobile", "parent_mobile", "phone", "mobile_number"))).filter(s -> !s.isBlank()).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Notification buildNotification(AppUser parent, FeeReminderRow row) {
        Notification notification = new Notification();
        notification.setUserId(parent.getId()); notification.setRole("PARENT"); notification.setSchoolId(parent.getSchoolId());
        notification.setClassName(row.getClassName()); notification.setSection(row.getSection()); notification.setType("FEE_REMINDER");
        notification.setTitle("Fee Reminder");
        notification.setMessage("Pending fee for " + safe(row.getStudentName(), "student") + ": ₹" + row.getPendingAmount() + ". Due date: " + row.getDueDate() + (blank(row.getRemarks()) ? "." : ". " + row.getRemarks()));
        notification.setRead(false); notification.setCreatedAt(LocalDateTime.now()); return notification;
    }

    private FeeReminderHistory buildHistory(FeeReminderUpload upload, FeeReminderRow row, AppUser parent) {
        FeeReminderHistory history = new FeeReminderHistory();
        history.setUploadId(upload.getId()); history.setRowId(row.getId()); history.setSchoolId(upload.getSchoolId()); history.setStudentDbId(row.getMappedStudentDbId());
        history.setStudentId(row.getStudentId()); history.setStudentName(row.getStudentName()); history.setClassName(row.getClassName()); history.setSection(row.getSection());
        history.setParentUserId(parent.getId()); history.setParentName(parent.getDisplayName()); history.setPendingAmount(row.getPendingAmount()); history.setDueDate(row.getDueDate()); history.setRemarks(row.getRemarks());
        history.setStatus("SENT"); history.setChannel("IN_APP"); history.setSentAt(LocalDateTime.now()); return history;
    }

    private void refreshCounts(FeeReminderUpload upload, List<FeeReminderRow> rows) {
        upload.setTotalRows(rows.size()); upload.setReadyRows(count(rows, READY)); upload.setInvalidRows(count(rows, INVALID_ROW)); upload.setMissingStudentRows(count(rows, MISSING_STUDENT)); upload.setMissingParentMappingRows(count(rows, MISSING_PARENT_MAPPING)); upload.setSentRows(count(rows, SENT)); upload.setFailedRows(count(rows, "FAILED"));
        if (upload.getSentRows() > 0 && upload.getReadyRows() == 0) upload.setStatus("SENT"); else upload.setStatus("PREVIEW_READY");
        uploadRepository.save(upload);
    }
    private int count(List<FeeReminderRow> rows, String status){ return (int) rows.stream().filter(r -> status.equals(r.getStatus())).count(); }
    private FeeReminderUpload loadUpload(Long uploadId, String schoolId){ return uploadRepository.findById(uploadId).filter(u -> normalizeSchoolId(schoolId).equalsIgnoreCase(u.getSchoolId())).orElseThrow(() -> new IllegalArgumentException("Fee reminder upload not found for this school.")); }
    private FeeReminderDtos.Summary toSummary(FeeReminderUpload u){ return new FeeReminderDtos.Summary(u.getId(), u.getSchoolId(), u.getOriginalFilename(), u.getStatus(), u.getTotalRows(), u.getReadyRows(), u.getInvalidRows(), u.getMissingStudentRows(), u.getMissingParentMappingRows(), u.getSentRows(), u.getFailedRows(), u.getCreatedAt(), u.getSentAt()); }
    private FeeReminderDtos.Row toRow(FeeReminderRow r){ return new FeeReminderDtos.Row(r.getId(), r.getRowNumber(), r.getStudentId(), r.getStudentName(), r.getClassName(), r.getSection(), r.getPendingAmount(), r.getDueDate(), r.getRemarks(), r.getStatus(), r.getValidationMessage(), r.getMappedParentNames()); }
    private FeeReminderDtos.History toHistory(FeeReminderHistory h){ return new FeeReminderDtos.History(h.getId(), h.getUploadId(), h.getStudentId(), h.getStudentName(), h.getClassName(), h.getSection(), h.getParentName(), h.getPendingAmount(), h.getDueDate(), h.getRemarks(), h.getStatus(), h.getChannel(), h.getSentAt()); }
    private Map<String,Integer> headerMap(Row header){ Map<String,Integer> map = new HashMap<>(); if(header==null)return map; for(Cell c: header){ map.put(norm(cellText(c)), c.getColumnIndex()); } return map; }
    private boolean isBlankRow(Row row){ for(Cell c: row){ if(!cellText(c).isBlank()) return false; } return true; }
    private String text(Row row, Map<String,Integer> cols, String... names){ for(String n:names){ Integer idx=cols.get(norm(n)); if(idx!=null) return cellText(row.getCell(idx)); } return ""; }
    private String cellText(Cell c){ if(c==null)return ""; return switch(c.getCellType()){ case STRING -> c.getStringCellValue().trim(); case NUMERIC -> DateUtil.isCellDateFormatted(c) ? c.getLocalDateTimeCellValue().toLocalDate().toString() : BigDecimal.valueOf(c.getNumericCellValue()).stripTrailingZeros().toPlainString(); case BOOLEAN -> String.valueOf(c.getBooleanCellValue()); case FORMULA -> c.getCellFormula(); default -> ""; }; }
    private BigDecimal amount(String s){ try { return blank(s) ? null : new BigDecimal(s.replace(",", "").replace("₹", "").trim()); } catch(Exception e){ return null; } }
    private LocalDate date(Row row, Map<String,Integer> cols, String... names){ for(String n:names){ Integer idx=cols.get(norm(n)); if(idx!=null){ Cell c=row.getCell(idx); if(c!=null && c.getCellType()==CellType.NUMERIC && DateUtil.isCellDateFormatted(c)) return c.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(); String s=cellText(c); for(String p: List.of("yyyy-MM-dd","MM/dd/yyyy","dd/MM/yyyy","dd-MM-yyyy")){ try{return LocalDate.parse(s, DateTimeFormatter.ofPattern(p));}catch(Exception ignored){} } } } return null; }
    private Map<String,String> jsonMap(String json){ try{return objectMapper.readValue(json, new TypeReference<Map<String,String>>(){});}catch(Exception e){return Map.of();} }
    private String first(Map<String,String> m, String... keys){ for(String k:keys){ for(var e:m.entrySet()){ if(norm(e.getKey()).equals(norm(k)) && e.getValue()!=null && !e.getValue().isBlank()) return e.getValue(); } } return ""; }
    private String normalizeSchoolId(String schoolId){ return blank(schoolId) ? "TST2" : schoolId.trim().toUpperCase(Locale.ROOT); }
    private String norm(String s){ return s == null ? "" : s.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", ""); }
    private String digits(String s){ return s == null ? "" : s.replaceAll("\\D+", ""); }
    private boolean blank(String s){ return s == null || s.isBlank(); }
    private String safe(String value, String fallback){ return blank(value) ? fallback : value; }
}
