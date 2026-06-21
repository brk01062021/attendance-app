package com.school.attendance.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.attendance.dto.AuthResponse;
import com.school.attendance.dto.ChangePasswordRequest;
import com.school.attendance.dto.LoginRequest;
import com.school.attendance.dto.ParentActivateRequest;
import com.school.attendance.dto.ParentOtpRequest;
import com.school.attendance.dto.ParentOtpResponse;
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
import com.school.attendance.service.WorkbookUserProvisioningService;
import com.school.attendance.service.notification.SmsOtpService;
import com.school.attendance.tenant.TenantContext;
import com.school.attendance.tenant.TenantUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.time.LocalDateTime;
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
    private final SmsOtpService smsOtpService;
    private static final SecureRandom OTP_RANDOM = new SecureRandom();

    private final ObjectMapper objectMapper;

    public AuthController(AppUserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil,
                          SchoolRegistrationService schoolRegistrationService,
                          SchoolImportUploadRepository schoolImportUploadRepository,
                          SchoolImportStagingRecordRepository stagingRecordRepository,
                          SmsOtpService smsOtpService,
                          ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.schoolRegistrationService = schoolRegistrationService;
        this.schoolImportUploadRepository = schoolImportUploadRepository;
        this.stagingRecordRepository = stagingRecordRepository;
        this.smsOtpService = smsOtpService;
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
    @Transactional
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
            if ("PARENT".equals(SecurityAccess.normalizeRole(user.getRole()))) {
                throw new RuntimeException("Parent account is not activated. Please use Student ID + parent mobile OTP setup first.");
            }
            throw new RuntimeException("Credentials are inactive. Please contact VidyaSetu Onboarding Team for reset/regeneration.");
        }

        String schoolCode = TenantUtils.normalizeOrDefault(user.getSchoolCode());
        TenantContext.setSchoolId(schoolCode);

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole(), schoolCode, user.getId());

        ParentChildInfo parentChildInfo = resolveParentChildInfo(user, schoolCode);

        String normalizedRole = SecurityAccess.normalizeRole(user.getRole());
        boolean mustChangePassword = Boolean.TRUE.equals(user.getForcePasswordChange())
                || isWorkbookTemporaryCredentialLogin(user, request.getPassword(), normalizedRole);
        if (mustChangePassword && !Boolean.TRUE.equals(user.getForcePasswordChange())) {
            user.setForcePasswordChange(true);
            userRepository.save(user);
        }

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
                normalizedRole,
                mustChangePassword,
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


    @PostMapping("/parent/request-otp")
    @Transactional
    public ParentOtpResponse requestParentOtp(@RequestBody ParentOtpRequest request) {
        String schoolCode = TenantUtils.normalizeOrDefault(request.getSchoolId());
        String studentId = normalizeStudentId(request.getStudentId());
        String parentMobile = normalizeParentMobileForIndia(request.getParentMobile());

        if (studentId.isBlank() || parentMobile.isBlank()) {
            throw new RuntimeException("School ID, Student ID and registered 10-digit Indian parent mobile number are required. Do not enter the Twilio sender number here.");
        }

        ParentChildInfo mappedStudent = validateParentStudentMapping(schoolCode, studentId, parentMobile);
        AppUser parentUser = userRepository.findByUsernameAndSchoolCodeIgnoreCase(parentMobile, schoolCode)
                .orElseThrow(() -> new RuntimeException("Parent mobile is not provisioned for this school import."));

        if (!"PARENT".equals(SecurityAccess.normalizeRole(parentUser.getRole()))) {
            throw new RuntimeException("The provided mobile number is not a parent login for this school.");
        }

        String otp = String.format("%06d", OTP_RANDOM.nextInt(1_000_000));
        smsOtpService.sendParentOtp(parentMobile, otp, schoolCode, studentId);

        parentUser.setParentOtpHash(passwordEncoder.encode(otp));
        parentUser.setParentOtpExpiresAt(LocalDateTime.now().plusMinutes(10));
        userRepository.save(parentUser);

        return new ParentOtpResponse(
                true,
                "OTP sent to registered parent mobile " + maskMobile(parentMobile) + ". Enter the received OTP to create the parent password.",
                schoolCode,
                studentId,
                parentMobile,
                maskMobile(parentMobile),
                null
        );
    }

    @PostMapping("/parent/activate")
    @Transactional
    public AuthResponse activateParent(@RequestBody ParentActivateRequest request) {
        String schoolCode = TenantUtils.normalizeOrDefault(request.getSchoolId());
        String studentId = normalizeStudentId(request.getStudentId());
        String parentMobile = normalizeParentMobileForIndia(request.getParentMobile());
        String otp = request.getOtp() == null ? "" : request.getOtp().trim();
        String newPassword = request.getNewPassword() == null ? "" : request.getNewPassword().trim();

        if (studentId.isBlank() || parentMobile.isBlank() || otp.isBlank()) {
            throw new RuntimeException("School ID, Student ID, parent mobile number and OTP are required.");
        }
        if (newPassword.length() < 8) {
            throw new RuntimeException("New password must be at least 8 characters.");
        }

        validateParentStudentMapping(schoolCode, studentId, parentMobile);
        AppUser parentUser = userRepository.findByUsernameAndSchoolCodeIgnoreCase(parentMobile, schoolCode)
                .orElseThrow(() -> new RuntimeException("Parent mobile is not provisioned for this school import."));

        if (!"PARENT".equals(SecurityAccess.normalizeRole(parentUser.getRole()))) {
            throw new RuntimeException("The provided mobile number is not a parent login for this school.");
        }
        if (parentUser.getParentOtpHash() == null || parentUser.getParentOtpExpiresAt() == null) {
            throw new RuntimeException("Please request a fresh OTP before activating parent login.");
        }
        if (parentUser.getParentOtpExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired. Please request a new OTP.");
        }
        if (!passwordEncoder.matches(otp, parentUser.getParentOtpHash())) {
            throw new RuntimeException("Invalid OTP. Please verify and try again.");
        }

        parentUser.setPassword(passwordEncoder.encode(newPassword));
        parentUser.setCredentialsActive(true);
        parentUser.setForcePasswordChange(false);
        parentUser.setParentOnboardingVerified(true);
        parentUser.setParentOtpHash(null);
        parentUser.setParentOtpExpiresAt(null);
        userRepository.save(parentUser);

        TenantContext.setSchoolId(schoolCode);
        String token = jwtUtil.generateToken(parentUser.getUsername(), parentUser.getRole(), schoolCode, parentUser.getId());
        ParentChildInfo parentChildInfo = resolveParentChildInfo(parentUser, schoolCode);

        return new AuthResponse(
                token,
                parentUser.getId(),
                parentUser.getSchoolId(),
                schoolCode,
                parentUser.getTeacherId(),
                parentUser.getTeacherName(),
                parentChildInfo.studentId(),
                parentChildInfo.studentName(),
                parentUser.getDisplayName(),
                parentUser.getSchoolName(),
                SecurityAccess.normalizeRole(parentUser.getRole()),
                false,
                true
        );
    }

    private boolean isWorkbookTemporaryCredentialLogin(AppUser user, String submittedPassword, String normalizedRole) {
        if (!("TEACHER".equals(normalizedRole) || "STUDENT".equals(normalizedRole))) {
            return false;
        }
        if (submittedPassword == null || submittedPassword.isBlank()) {
            return false;
        }
        return passwordEncoder.matches(WorkbookUserProvisioningService.DEFAULT_TEMP_PASSWORD, user.getPassword())
                && WorkbookUserProvisioningService.DEFAULT_TEMP_PASSWORD.equals(submittedPassword);
    }

    private ParentChildInfo resolveParentChildInfo(AppUser user, String schoolCode) {
        String role = SecurityAccess.normalizeRole(user.getRole());

        if ("STUDENT".equals(role)) {
            return new ParentChildInfo(user.getId(), user.getDisplayName());
        }

        if (!"PARENT".equals(role)) {
            return ParentChildInfo.empty();
        }

        String parentMobile = normalizeParentMobileForIndia(user.getUsername());
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
                .filter(values -> normalizeParentMobileForIndia(value(values, "mobile")).equals(parentMobile))
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


    private ParentChildInfo validateParentStudentMapping(String schoolCode, String studentId, String parentMobile) {
        Long uploadId = schoolImportUploadRepository
                .findFirstBySchoolCodeIgnoreCaseAndCommittedTrueAndRolledBackFalseOrderByCommittedAtDesc(schoolCode)
                .map(SchoolImportUpload::getId)
                .orElseThrow(() -> new RuntimeException("No committed school import found for this school."));

        List<SchoolImportStagingRecord> rows = stagingRecordRepository.findByUploadId(uploadId);
        boolean parentMapped = rows.stream()
                .filter(row -> isSheet(row, "Parents"))
                .map(this::values)
                .anyMatch(values -> normalizeParentMobileForIndia(value(values, "mobile")).equals(parentMobile)
                        && normalizeStudentId(firstNonBlank(value(values, "admission_no"), value(values, "student_id"), value(values, "student_admission_no"))).equals(studentId));

        if (!parentMapped) {
            throw new RuntimeException("Student ID and parent mobile number do not match the committed school import data.");
        }

        return rows.stream()
                .filter(row -> isSheet(row, "Students"))
                .map(this::values)
                .filter(values -> normalizeStudentId(firstNonBlank(value(values, "admission_no"), value(values, "student_id"))).equals(studentId))
                .map(values -> new ParentChildInfo(null, firstNonBlank(value(values, "student_name"), value(values, "name"), studentId)))
                .findFirst()
                .orElse(new ParentChildInfo(null, studentId));
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
        return mobile == null ? "" : mobile.replaceAll("[^0-9+]", "").trim();
    }

    private String normalizeParentMobileForIndia(String mobile) {
        String digits = mobile == null ? "" : mobile.replaceAll("\\D", "").trim();
        if (digits.isBlank()) {
            return "";
        }
        if (digits.length() == 12 && digits.startsWith("91")) {
            digits = digits.substring(2);
        } else if (digits.length() == 11 && digits.startsWith("0")) {
            digits = digits.substring(1);
        }
        if (digits.length() != 10 || !digits.matches("[6-9]\\d{9}")) {
            return "";
        }
        return digits;
    }

    private String normalizeStudentId(String studentId) {
        return studentId == null ? "" : studentId.trim().toUpperCase(Locale.ROOT);
    }

    private String maskMobile(String mobile) {
        String clean = normalizeMobile(mobile);
        if (clean.length() <= 4) return "****";
        return "****" + clean.substring(clean.length() - 4);
    }

    private record ParentChildInfo(Long studentId, String studentName) {
        static ParentChildInfo empty() {
            return new ParentChildInfo(null, null);
        }
    }

}
