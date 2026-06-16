package com.school.attendance.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.attendance.dto.AuthResponse;
import com.school.attendance.dto.ChangePasswordRequest;
import com.school.attendance.dto.LoginRequest;
import com.school.attendance.dto.RegisterRequest;
import com.school.attendance.entity.AppUser;
import com.school.attendance.entity.SchoolImportStagingRecord;
import com.school.attendance.entity.SchoolImportUpload;
import com.school.attendance.repository.AppUserRepository;
import com.school.attendance.repository.SchoolImportStagingRecordRepository;
import com.school.attendance.repository.SchoolImportUploadRepository;
import com.school.attendance.security.JwtUtil;
import com.school.attendance.security.SecurityAccess;
import com.school.attendance.service.onboarding.SchoolRegistrationService;
import com.school.attendance.tenant.TenantContext;
import com.school.attendance.tenant.TenantUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SchoolRegistrationService schoolRegistrationService;
    private final SchoolImportUploadRepository schoolImportUploadRepository;
    private final SchoolImportStagingRecordRepository stagingRecordRepository;
    private final ObjectMapper objectMapper;

    public AuthController(AppUserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil,
                          SchoolRegistrationService schoolRegistrationService,
                          SchoolImportUploadRepository schoolImportUploadRepository,
                          SchoolImportStagingRecordRepository stagingRecordRepository,
                          ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.schoolRegistrationService = schoolRegistrationService;
        this.schoolImportUploadRepository = schoolImportUploadRepository;
        this.stagingRecordRepository = stagingRecordRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        String schoolCode = TenantUtils.normalizeOrDefault(request.getSchoolId());

        if (userRepository.findByUsernameAndSchoolCodeIgnoreCase(request.getUsername(), schoolCode).isPresent()) {
            return "Username already exists for school " + schoolCode;
        }

        AppUser user = new AppUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(SecurityAccess.normalizeRole(request.getRole()));
        user.setTeacherId(request.getTeacherId());
        user.setTeacherName(request.getTeacherName());
        user.setSchoolId(1L);
        user.setSchoolCode(schoolCode);
        user.setDisplayName(request.getTeacherName() != null && !request.getTeacherName().isBlank()
                ? request.getTeacherName()
                : request.getUsername());
        user.setSchoolName("BRK International School");

        userRepository.save(user);

        return "User registered successfully";
    }

    @PostMapping("/login")
    @Transactional(readOnly = true)
    public AuthResponse login(@RequestBody LoginRequest request) {
        String requestedSchoolCode = TenantUtils.normalizeOrDefault(request.getSchoolId());

        if (!schoolRegistrationService.isLoginEnabledForSchoolId(requestedSchoolCode)) {
            throw new RuntimeException("School registration is not active yet. Please check registration status using your reference ID.");
        }

        AppUser user = userRepository.findByUsernameAndSchoolCodeIgnoreCase(request.getUsername(), requestedSchoolCode)
                .orElseThrow(() -> new RuntimeException("Invalid username, password, or school ID"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username, password, or school ID");
        }

        if (!Boolean.TRUE.equals(user.getCredentialsActive())) {
            throw new RuntimeException("Credentials are inactive. Please contact VidyaSetu Onboarding Team for reset/regeneration.");
        }

        String schoolCode = TenantUtils.normalizeOrDefault(user.getSchoolCode());
        TenantContext.setSchoolId(schoolCode);

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole(), schoolCode, user.getId());

        ParentChildInfo parentChildInfo = resolveParentChildInfo(user, schoolCode);

        return new AuthResponse(
                token,
                user.getId(),
                user.getSchoolId(),
                schoolCode,
                user.getTeacherId(),
                user.getTeacherName(),
                parentChildInfo.studentId(),
                parentChildInfo.studentName(),
                user.getDisplayName(),
                user.getSchoolName(),
                SecurityAccess.normalizeRole(user.getRole()),
                Boolean.TRUE.equals(user.getForcePasswordChange()),
                Boolean.TRUE.equals(user.getCredentialsActive())
        );
    }

    @PostMapping("/change-password")
    @Transactional
    public AuthResponse changePassword(@RequestBody ChangePasswordRequest request) {
        String requestedSchoolCode = TenantUtils.normalizeOrDefault(request.getSchoolId());

        AppUser user = userRepository.findByUsernameAndSchoolCodeIgnoreCase(request.getUsername(), requestedSchoolCode)
                .orElseThrow(() -> new RuntimeException("Invalid username, password, or school ID"));

        if (!Boolean.TRUE.equals(user.getCredentialsActive())) {
            throw new RuntimeException("Credentials are inactive. Please contact VidyaSetu Onboarding Team for reset/regeneration.");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect.");
        }

        String newPassword = request.getNewPassword() == null ? "" : request.getNewPassword().trim();
        if (newPassword.length() < 8) {
            throw new RuntimeException("New password must be at least 8 characters.");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("New password must be different from temporary password.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setForcePasswordChange(false);
        user.setCredentialsActive(true);
        userRepository.save(user);

        String schoolCode = TenantUtils.normalizeOrDefault(user.getSchoolCode());
        TenantContext.setSchoolId(schoolCode);
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole(), schoolCode, user.getId());

        ParentChildInfo parentChildInfo = resolveParentChildInfo(user, schoolCode);

        return new AuthResponse(
                token,
                user.getId(),
                user.getSchoolId(),
                schoolCode,
                user.getTeacherId(),
                user.getTeacherName(),
                parentChildInfo.studentId(),
                parentChildInfo.studentName(),
                user.getDisplayName(),
                user.getSchoolName(),
                SecurityAccess.normalizeRole(user.getRole()),
                false,
                true
        );
    }


    private ParentChildInfo resolveParentChildInfo(AppUser user, String schoolCode) {
        String role = SecurityAccess.normalizeRole(user.getRole());

        if ("STUDENT".equals(role)) {
            return new ParentChildInfo(user.getId(), user.getDisplayName());
        }

        if (!"PARENT".equals(role)) {
            return ParentChildInfo.empty();
        }

        String parentMobile = normalizeMobile(user.getUsername());
        if (parentMobile.isBlank()) {
            return ParentChildInfo.empty();
        }

        return schoolImportUploadRepository
                .findFirstBySchoolCodeIgnoreCaseAndCommittedTrueAndRolledBackFalseOrderByCommittedAtDesc(schoolCode)
                .map(SchoolImportUpload::getId)
                .map(uploadId -> resolveParentChildFromStaging(uploadId, parentMobile))
                .orElse(ParentChildInfo.empty());
    }

    private ParentChildInfo resolveParentChildFromStaging(Long uploadId, String parentMobile) {
        List<SchoolImportStagingRecord> rows = stagingRecordRepository.findByUploadId(uploadId);
        String admissionNo = rows.stream()
                .filter(row -> isSheet(row, "Parents"))
                .map(this::values)
                .filter(values -> normalizeMobile(value(values, "mobile")).equals(parentMobile))
                .map(values -> firstNonBlank(value(values, "admission_no"), value(values, "student_id"), value(values, "student_admission_no")))
                .filter(value -> !value.isBlank())
                .findFirst()
                .orElse("");

        if (admissionNo.isBlank()) {
            return ParentChildInfo.empty();
        }

        String normalizedAdmissionNo = admissionNo.trim().toUpperCase(Locale.ROOT);
        return rows.stream()
                .filter(row -> isSheet(row, "Students"))
                .map(this::values)
                .filter(values -> value(values, "admission_no").trim().equalsIgnoreCase(normalizedAdmissionNo))
                .map(values -> new ParentChildInfo(null, firstNonBlank(value(values, "student_name"), value(values, "name"), normalizedAdmissionNo)))
                .findFirst()
                .orElse(new ParentChildInfo(null, normalizedAdmissionNo));
    }

    private boolean isSheet(SchoolImportStagingRecord row, String sheetName) {
        return row != null && row.getSheetName() != null && row.getSheetName().equalsIgnoreCase(sheetName);
    }

    private Map<String, String> values(SchoolImportStagingRecord row) {
        try {
            if (row.getRowJson() == null || row.getRowJson().isBlank()) return Map.of();
            Map<String, String> parsed = objectMapper.readValue(row.getRowJson(), new TypeReference<>() {});
            return parsed.entrySet().stream().collect(java.util.stream.Collectors.toMap(
                    entry -> entry.getKey() == null ? "" : entry.getKey().trim().toLowerCase(Locale.ROOT),
                    entry -> entry.getValue() == null ? "" : entry.getValue().trim(),
                    (first, ignored) -> first
            ));
        } catch (Exception ignored) {
            return Map.of();
        }
    }

    private String value(Map<String, String> values, String key) {
        if (values == null || key == null) return "";
        return values.getOrDefault(key.toLowerCase(Locale.ROOT), "").trim();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return value.trim();
        }
        return "";
    }

    private String normalizeMobile(String mobile) {
        return mobile == null ? "" : mobile.replaceAll("\\s+", "").trim();
    }

    private record ParentChildInfo(Long studentId, String studentName) {
        static ParentChildInfo empty() {
            return new ParentChildInfo(null, null);
        }
    }

}
