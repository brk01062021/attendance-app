package com.school.attendance.service.provisioning;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.attendance.dto.provisioning.UserProvisioningCredentialDTO;
import com.school.attendance.dto.provisioning.UserProvisioningResponseDTO;
import com.school.attendance.entity.AppUser;
import com.school.attendance.entity.SchoolImportStagingRecord;
import com.school.attendance.entity.SchoolImportUpload;
import com.school.attendance.repository.AppUserRepository;
import com.school.attendance.repository.SchoolImportStagingRecordRepository;
import com.school.attendance.repository.SchoolImportUploadRepository;
import com.school.attendance.security.SecurityAccess;
import com.school.attendance.tenant.TenantUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserProvisioningService {
    private final SchoolImportUploadRepository uploadRepository;
    private final SchoolImportStagingRecordRepository stagingRepository;
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public UserProvisioningService(SchoolImportUploadRepository uploadRepository,
                                   SchoolImportStagingRecordRepository stagingRepository,
                                   AppUserRepository userRepository,
                                   PasswordEncoder passwordEncoder,
                                   ObjectMapper objectMapper) {
        this.uploadRepository = uploadRepository;
        this.stagingRepository = stagingRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public UserProvisioningResponseDTO generateFromLatestCommittedWorkbook(String schoolId) {
        String safeSchoolId = TenantUtils.normalizeOrDefault(schoolId);
        SchoolImportUpload upload = uploadRepository
                .findFirstBySchoolCodeIgnoreCaseAndCommittedTrueAndRolledBackFalseOrderByCommittedAtDesc(safeSchoolId)
                .orElseThrow(() -> new IllegalStateException("Commit a valid school workbook before provisioning users."));

        List<SchoolImportStagingRecord> rows = stagingRepository.findByUploadId(upload.getId());
        if (rows.isEmpty()) {
            throw new IllegalStateException("Committed workbook has no staged rows. Recommit the workbook before provisioning users.");
        }

        List<UserProvisioningCredentialDTO> credentials = new ArrayList<>();
        provisionPrincipal(safeSchoolId, credentials);
        for (SchoolImportStagingRecord row : rows) {
            String sheet = row.getSheetName() == null ? "" : row.getSheetName().trim().toLowerCase(Locale.ROOT);
            Map<String, String> values = readValues(row.getRowJson());
            if ("teachers".equals(sheet)) {
                provisionTeacher(safeSchoolId, values, credentials);
            } else if ("students".equals(sheet)) {
                provisionStudent(safeSchoolId, values, credentials);
            }
        }

        int principalCount = count(credentials, "PRINCIPAL");
        int teacherCount = count(credentials, "TEACHER");
        int studentCount = count(credentials, "STUDENT");
        int parentCount = count(credentials, "PARENT");
        int createdCount = (int) credentials.stream().filter(UserProvisioningCredentialDTO::isCreated).count();
        int updatedCount = (int) credentials.stream().filter(UserProvisioningCredentialDTO::isUpdated).count();
        return new UserProvisioningResponseDTO(
                safeSchoolId,
                upload.getId(),
                upload.getImportBatchId(),
                principalCount,
                teacherCount,
                studentCount,
                parentCount,
                createdCount,
                updatedCount,
                "Teacher and student provisioning completed from the latest committed workbook. Parents are excluded from temporary credentials and must activate using Student ID + parent mobile OTP.",
                credentials
        );
    }

    @Transactional(readOnly = true)
    public List<UserProvisioningCredentialDTO> downloadableCredentials(String schoolId, String requestedRole) {
        String safeSchoolId = TenantUtils.normalizeOrDefault(schoolId);
        String role = SecurityAccess.normalizeRole(requestedRole);
        if (!Set.of("TEACHER", "STUDENT").contains(role)) {
            throw new IllegalArgumentException("Only TEACHER and STUDENT credentials can be downloaded. Parents must activate by OTP.");
        }

        List<UserProvisioningCredentialDTO> credentials = loadCredentialsFromCommittedImport(safeSchoolId, role);
        if (!credentials.isEmpty()) {
            return credentials;
        }

        return loadCredentialsFromProvisionedUsers(safeSchoolId, role);
    }

    private List<UserProvisioningCredentialDTO> loadCredentialsFromCommittedImport(String schoolId, String role) {
        Optional<SchoolImportUpload> latestUpload = uploadRepository
                .findFirstBySchoolCodeIgnoreCaseAndCommittedTrueAndRolledBackFalseOrderByCommittedAtDesc(schoolId);
        if (latestUpload.isEmpty()) {
            return List.of();
        }

        List<UserProvisioningCredentialDTO> credentials = new ArrayList<>();
        List<SchoolImportStagingRecord> rows = stagingRepository.findByUploadId(latestUpload.get().getId());
        for (SchoolImportStagingRecord row : rows) {
            String sheet = row.getSheetName() == null ? "" : row.getSheetName().trim().toLowerCase(Locale.ROOT);
            Map<String, String> values = readValues(row.getRowJson());
            if ("TEACHER".equals(role) && "teachers".equals(sheet)) {
                String teacherId = firstNonBlank(values, "teacher_id", "teacherid", "id");
                String teacherName = firstNonBlank(values, "teacher_name", "name", "full_name");
                if (!teacherId.isBlank() || !teacherName.isBlank()) {
                    String username = !teacherId.isBlank() ? teacherId.trim().toUpperCase(Locale.ROOT) : slug(teacherName);
                    credentials.add(new UserProvisioningCredentialDTO("TEACHER", username, com.school.attendance.service.WorkbookUserProvisioningService.DEFAULT_TEMP_PASSWORD, firstNonBlank(teacherName, username), firstNonBlank(teacherId, teacherName), false, false));
                }
            } else if ("STUDENT".equals(role) && "students".equals(sheet)) {
                String admissionNo = firstNonBlank(values, "admission_no", "student_id", "studentid");
                String studentName = firstNonBlank(values, "student_name", "name", "full_name");
                if (!admissionNo.isBlank()) {
                    credentials.add(new UserProvisioningCredentialDTO("STUDENT", admissionNo.trim().toUpperCase(Locale.ROOT), com.school.attendance.service.WorkbookUserProvisioningService.DEFAULT_TEMP_PASSWORD, firstNonBlank(studentName, admissionNo), admissionNo, false, false));
                }
            }
        }
        return credentials;
    }

    private List<UserProvisioningCredentialDTO> loadCredentialsFromProvisionedUsers(String schoolId, String role) {
        String tempPassword = "TEACHER".equals(role)
                ? com.school.attendance.service.WorkbookUserProvisioningService.DEFAULT_TEMP_PASSWORD
                : com.school.attendance.service.WorkbookUserProvisioningService.DEFAULT_TEMP_PASSWORD;
        return userRepository.findByRoleIgnoreCaseAndSchoolCodeIgnoreCase(role, schoolId).stream()
                .filter(user -> Boolean.TRUE.equals(user.getCredentialsActive()))
                .sorted(Comparator.comparing(user -> firstNonBlank(user.getUsername(), user.getDisplayName()), String.CASE_INSENSITIVE_ORDER))
                .map(user -> new UserProvisioningCredentialDTO(
                        role,
                        user.getUsername(),
                        tempPassword,
                        firstNonBlank(user.getDisplayName(), user.getTeacherName(), user.getUsername()),
                        firstNonBlank(user.getUsername(), user.getDisplayName()),
                        false,
                        false
                ))
                .toList();
    }


    public UserProvisioningResponseDTO summary(String schoolId) {
        String safeSchoolId = TenantUtils.normalizeOrDefault(schoolId);
        List<AppUser> users = userRepository.findBySchoolCodeIgnoreCase(safeSchoolId);
        int principalCount = (int) users.stream().filter(u -> sameRole(u, "PRINCIPAL")).count();
        int teacherCount = (int) users.stream().filter(u -> sameRole(u, "TEACHER")).count();
        int studentCount = (int) users.stream().filter(u -> sameRole(u, "STUDENT")).count();
        int parentCount = (int) users.stream().filter(u -> sameRole(u, "PARENT")).count();
        return new UserProvisioningResponseDTO(safeSchoolId, null, null, principalCount, teacherCount, studentCount, parentCount, 0, 0, "Current provisioned user summary for school " + safeSchoolId + ".", List.of());
    }

    private void provisionPrincipal(String schoolId, List<UserProvisioningCredentialDTO> credentials) {
        upsertUser(schoolId, "principal", "Principal@123", "PRINCIPAL", null, "Principal User", "principal", credentials);
    }

    private void provisionTeacher(String schoolId, Map<String, String> values, List<UserProvisioningCredentialDTO> credentials) {
        String teacherId = firstNonBlank(values, "teacher_id", "teacherid", "id");
        String teacherName = firstNonBlank(values, "teacher_name", "name", "full_name");
        if (teacherId.isBlank() && teacherName.isBlank()) return;
        String username = !teacherId.isBlank() ? teacherId.trim().toLowerCase(Locale.ROOT) : slug(teacherName);
        upsertUser(schoolId, username, "Teacher@123", "TEACHER", parseLong(teacherId), firstNonBlank(teacherName, username), firstNonBlank(teacherId, teacherName), credentials);
    }

    private void provisionStudent(String schoolId, Map<String, String> values, List<UserProvisioningCredentialDTO> credentials) {
        String admissionNo = firstNonBlank(values, "admission_no", "student_id", "studentid");
        String studentName = firstNonBlank(values, "student_name", "name", "full_name");
        if (admissionNo.isBlank()) return;
        upsertUser(schoolId, admissionNo.trim().toUpperCase(Locale.ROOT), "Student@123", "STUDENT", null, firstNonBlank(studentName, admissionNo), admissionNo, credentials);
    }

    private void provisionParent(String schoolId, Map<String, String> values, List<UserProvisioningCredentialDTO> credentials) {
        String mobile = digits(firstNonBlank(values, "mobile", "parent_mobile", "phone", "mobile_number"));
        String parentName = firstNonBlank(values, "parent_name", "name", "father_name", "mother_name");
        String admissionNo = firstNonBlank(values, "admission_no", "student_id");
        if (mobile.isBlank()) return;
        upsertUser(schoolId, mobile, "Parent@123", "PARENT", null, firstNonBlank(parentName, "Parent " + mobile), firstNonBlank(admissionNo, mobile), credentials);
    }

    private void upsertUser(String schoolId, String username, String temporaryPassword, String role, Long teacherId, String displayName, String linkedReference, List<UserProvisioningCredentialDTO> credentials) {
        if (username == null || username.isBlank()) return;
        Optional<AppUser> existing = userRepository.findByUsernameAndSchoolCodeIgnoreCase(username, schoolId);
        AppUser user = existing.orElseGet(AppUser::new);
        boolean created = existing.isEmpty();
        user.setUsername(username.trim());
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setRole(SecurityAccess.normalizeRole(role));
        user.setTeacherId(teacherId);
        user.setTeacherName("TEACHER".equals(role) ? displayName : null);
        user.setSchoolId(1L);
        user.setSchoolCode(schoolId);
        user.setDisplayName(displayName);
        user.setSchoolName("VidyaSetu School " + schoolId);
        user.setCredentialsActive(true);
        user.setForcePasswordChange(true);
        userRepository.save(user);
        credentials.add(new UserProvisioningCredentialDTO(role, username.trim(), temporaryPassword, displayName, linkedReference, created, !created));
    }

    private Map<String, String> readValues(String rowJson) {
        try {
            Map<String, String> raw = objectMapper.readValue(rowJson, new TypeReference<>() {});
            Map<String, String> normalized = new HashMap<>();
            raw.forEach((key, value) -> normalized.put(normalizeKey(key), value == null ? "" : value.trim()));
            return normalized;
        } catch (Exception e) {
            return Map.of();
        }
    }

    private String firstNonBlank(Map<String, String> values, String... keys) {
        for (String key : keys) {
            String value = values.get(normalizeKey(key));
            if (value != null && !value.isBlank()) return value.trim();
        }
        return "";
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return value.trim();
        }
        return "";
    }

    private String normalizeKey(String key) {
        return key == null ? "" : key.trim().toLowerCase(Locale.ROOT).replace(" ", "_").replace("-", "_");
    }

    private Long parseLong(String value) {
        try {
            if (value == null || value.isBlank()) return null;
            return Long.parseLong(value.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return null;
        }
    }

    private String slug(String value) {
        String slug = value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", ".").replaceAll("^\\.|\\.$", "");
        return slug.isBlank() ? "user" + System.currentTimeMillis() : slug;
    }

    private String digits(String value) {
        return value == null ? "" : value.replaceAll("[^0-9]", "");
    }

    private boolean sameRole(AppUser user, String role) {
        return role.equalsIgnoreCase(SecurityAccess.normalizeRole(user.getRole()));
    }

    private int count(List<UserProvisioningCredentialDTO> credentials, String role) {
        return (int) credentials.stream().filter(c -> role.equalsIgnoreCase(c.getRole())).count();
    }
}
