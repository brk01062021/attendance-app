package com.school.attendance.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.attendance.entity.AppUser;
import com.school.attendance.entity.SchoolImportStagingRecord;
import com.school.attendance.entity.SchoolImportUpload;
import com.school.attendance.entity.Student;
import com.school.attendance.entity.TeacherAssignment;
import com.school.attendance.repository.AppUserRepository;
import com.school.attendance.repository.SchoolImportStagingRecordRepository;
import com.school.attendance.repository.StudentRepository;
import com.school.attendance.repository.TeacherAssignmentRepository;
import com.school.attendance.security.SecurityAccess;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class WorkbookUserProvisioningService {

    public static final String DEFAULT_TEMP_PASSWORD = "Welcome@123";

    private final SchoolImportStagingRecordRepository stagingRepository;
    private final StudentRepository studentRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public WorkbookUserProvisioningService(SchoolImportStagingRecordRepository stagingRepository,
                                           StudentRepository studentRepository,
                                           TeacherAssignmentRepository teacherAssignmentRepository,
                                           AppUserRepository appUserRepository,
                                           PasswordEncoder passwordEncoder,
                                           ObjectMapper objectMapper) {
        this.stagingRepository = stagingRepository;
        this.studentRepository = studentRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ProvisioningResult provisionCommittedWorkbook(SchoolImportUpload upload) {
        if (upload == null || upload.getId() == null || !upload.isCommitted()) {
            return ProvisioningResult.empty();
        }

        List<SchoolImportStagingRecord> rows = stagingRepository.findByUploadId(upload.getId());
        if (rows.isEmpty()) {
            return ProvisioningResult.empty();
        }

        String schoolCode = normalizedSchoolCode(upload.getSchoolCode());
        String schoolName = resolveSchoolName(rows, upload.getSchoolCode());

        Map<String, Map<String, String>> teachersByWorkbookId = rows.stream()
                .filter(row -> sheet(row, "Teachers"))
                .map(this::values)
                .filter(values -> !value(values, "teacher_id").isBlank())
                .collect(Collectors.toMap(
                        values -> value(values, "teacher_id").toUpperCase(Locale.ROOT),
                        Function.identity(),
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));

        Map<String, Map<String, String>> studentsByAdmission = rows.stream()
                .filter(row -> sheet(row, "Students"))
                .map(this::values)
                .filter(values -> !value(values, "admission_no").isBlank())
                .collect(Collectors.toMap(
                        values -> value(values, "admission_no").toUpperCase(Locale.ROOT),
                        Function.identity(),
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));

        // Current student and teacher-assignment domain tables are legacy/global tables without school_code.
        // Rebuild them from the latest committed workbook so Day 28 role validation uses committed data only.
        studentRepository.deleteAllInBatch();
        teacherAssignmentRepository.deleteAllInBatch();

        List<Student> students = new ArrayList<>();
        for (Map<String, String> values : studentsByAdmission.values()) {
            Student student = new Student();
            student.setAdmissionNumber(value(values, "admission_no"));
            student.setName(firstNonBlank(value(values, "student_name"), value(values, "name"), student.getAdmissionNumber()));
            student.setClassName(value(values, "class_name"));
            student.setSection(value(values, "section"));
            student.setRollNumber(firstNonBlank(value(values, "roll_number"), value(values, "roll_no")));
            students.add(student);
        }
        studentRepository.saveAll(students);

        List<TeacherAssignment> assignments = new ArrayList<>();
        for (SchoolImportStagingRecord row : rows) {
            if (!sheet(row, "TeacherAssignments")) continue;
            Map<String, String> values = values(row);
            String teacherWorkbookId = value(values, "teacher_id");
            if (teacherWorkbookId.isBlank()) continue;
            Map<String, String> teacher = teachersByWorkbookId.getOrDefault(teacherWorkbookId.toUpperCase(Locale.ROOT), Map.of());
            TeacherAssignment assignment = new TeacherAssignment();
            assignment.setTeacherId(numericTeacherId(teacherWorkbookId));
            assignment.setTeacherName(firstNonBlank(value(teacher, "teacher_name"), value(values, "teacher_name"), teacherWorkbookId));
            assignment.setSubjectName(firstNonBlank(value(values, "subject"), value(values, "subject_name")));
            assignment.setClassName(value(values, "class_name"));
            assignment.setSection(value(values, "section"));
            assignments.add(assignment);
        }
        teacherAssignmentRepository.saveAll(assignments);

        // Recommit must be fully idempotent. Teacher, student, and parent accounts are generated
        // from the workbook, so before re-materializing them we remove every previously generated
        // workbook role user for this tenant in a single database operation. This also cleans up
        // duplicates that may have been created by older recommit logic while preserving Admin and
        // Principal accounts.
        Set<String> workbookGeneratedRoles = Set.of("TEACHER", "STUDENT", "PARENT");
        appUserRepository.deleteProvisionedRoleUsers(schoolCode, workbookGeneratedRoles);
        appUserRepository.flush();

        Map<String, AppUser> usersByTenantRoleUsername = new LinkedHashMap<>();
        for (Map<String, String> teacher : teachersByWorkbookId.values()) {
            String teacherWorkbookId = value(teacher, "teacher_id");
            putUser(usersByTenantRoleUsername, user(
                    teacherWorkbookId,
                    "TEACHER",
                    firstNonBlank(value(teacher, "teacher_name"), teacherWorkbookId),
                    schoolCode,
                    schoolName,
                    numericTeacherId(teacherWorkbookId),
                    firstNonBlank(value(teacher, "teacher_name"), teacherWorkbookId)
            ));
        }

        for (Map<String, String> student : studentsByAdmission.values()) {
            String admissionNo = value(student, "admission_no");
            putUser(usersByTenantRoleUsername, user(
                    admissionNo,
                    "STUDENT",
                    firstNonBlank(value(student, "student_name"), admissionNo),
                    schoolCode,
                    schoolName,
                    null,
                    null
            ));
        }

        Map<String, Map<String, String>> parentsByMobile = rows.stream()
                .filter(row -> sheet(row, "Parents"))
                .map(this::values)
                .filter(values -> !value(values, "mobile").isBlank())
                .collect(Collectors.toMap(
                        values -> normalizeMobile(value(values, "mobile")),
                        Function.identity(),
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
        for (Map<String, String> parent : parentsByMobile.values()) {
            String mobile = normalizeMobile(value(parent, "mobile"));
            putUser(usersByTenantRoleUsername, user(
                    mobile,
                    "PARENT",
                    firstNonBlank(value(parent, "parent_name"), mobile),
                    schoolCode,
                    schoolName,
                    null,
                    null
            ));
        }

        appUserRepository.saveAll(usersByTenantRoleUsername.values());
        return new ProvisioningResult(students.size(), assignments.size(), teachersByWorkbookId.size(), studentsByAdmission.size(), parentsByMobile.size());
    }

    private void putUser(Map<String, AppUser> usersByTenantRoleUsername, AppUser user) {
        if (user == null || user.getUsername() == null || user.getUsername().isBlank()) return;
        String key = normalizedSchoolCode(user.getSchoolCode())
                + "|" + SecurityAccess.normalizeRole(user.getRole())
                + "|" + user.getUsername().trim().toUpperCase(Locale.ROOT);
        usersByTenantRoleUsername.putIfAbsent(key, user);
    }

    private AppUser user(String username,
                         String role,
                         String displayName,
                         String schoolCode,
                         String schoolName,
                         Long teacherId,
                         String teacherName) {
        AppUser user = new AppUser();
        user.setUsername(username.trim());
        user.setPassword(passwordEncoder.encode(DEFAULT_TEMP_PASSWORD));
        user.setRole(role);
        user.setSchoolId(1L);
        user.setSchoolCode(schoolCode);
        user.setSchoolName(schoolName);
        user.setDisplayName(displayName);
        user.setTeacherId(teacherId);
        user.setTeacherName(teacherName);
        user.setCredentialsActive(true);
        user.setForcePasswordChange(true);
        return user;
    }

    private String resolveSchoolName(List<SchoolImportStagingRecord> rows, String fallback) {
        return rows.stream()
                .filter(row -> sheet(row, "SchoolProfile"))
                .map(this::values)
                .map(values -> value(values, "school_name"))
                .filter(value -> !value.isBlank())
                .findFirst()
                .orElse(firstNonBlank(fallback, "VidyaSetu School"));
    }

    private boolean sheet(SchoolImportStagingRecord row, String sheetName) {
        return row != null && row.getSheetName() != null && row.getSheetName().equalsIgnoreCase(sheetName);
    }

    private Map<String, String> values(SchoolImportStagingRecord row) {
        try {
            if (row.getRowJson() == null || row.getRowJson().isBlank()) return Map.of();
            Map<String, String> parsed = objectMapper.readValue(row.getRowJson(), new TypeReference<>() {});
            Map<String, String> normalized = new LinkedHashMap<>();
            parsed.forEach((key, value) -> normalized.put(key == null ? "" : key.trim().toLowerCase(Locale.ROOT), value == null ? "" : value.trim()));
            return normalized;
        } catch (Exception e) {
            return Map.of();
        }
    }

    private String value(Map<String, String> values, String key) {
        if (values == null || key == null) return "";
        return Optional.ofNullable(values.get(key.toLowerCase(Locale.ROOT))).orElse("").trim();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return value.trim();
        }
        return "";
    }

    private Long numericTeacherId(String teacherWorkbookId) {
        String digits = teacherWorkbookId == null ? "" : teacherWorkbookId.replaceAll("\\D+", "");
        if (digits.isBlank()) return null;
        try {
            return Long.parseLong(digits);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String normalizeMobile(String mobile) {
        return mobile == null ? "" : mobile.replaceAll("\\s+", "").trim();
    }

    private String normalizedSchoolCode(String schoolCode) {
        return schoolCode == null || schoolCode.isBlank() ? "DEMO" : schoolCode.trim().toUpperCase(Locale.ROOT);
    }

    public record ProvisioningResult(int studentsMaterialized,
                                     int teacherAssignmentsMaterialized,
                                     int teacherUsersCreated,
                                     int studentUsersCreated,
                                     int parentUsersCreated) {
        public static ProvisioningResult empty() {
            return new ProvisioningResult(0, 0, 0, 0, 0);
        }

        public int totalUsersCreated() {
            return teacherUsersCreated + studentUsersCreated + parentUsersCreated;
        }

        public boolean didProvision() {
            return studentsMaterialized > 0 || teacherAssignmentsMaterialized > 0 || totalUsersCreated() > 0;
        }
    }
}
