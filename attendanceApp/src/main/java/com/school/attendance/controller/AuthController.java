package com.school.attendance.controller;

import com.school.attendance.dto.AuthResponse;
import com.school.attendance.dto.ChangePasswordRequest;
import com.school.attendance.dto.LoginRequest;
import com.school.attendance.dto.RegisterRequest;
import com.school.attendance.entity.AppUser;
import com.school.attendance.repository.AppUserRepository;
import com.school.attendance.security.JwtUtil;
import com.school.attendance.security.SecurityAccess;
import com.school.attendance.service.onboarding.SchoolRegistrationService;
import com.school.attendance.tenant.TenantContext;
import com.school.attendance.tenant.TenantUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SchoolRegistrationService schoolRegistrationService;

    public AuthController(AppUserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil,
                          SchoolRegistrationService schoolRegistrationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.schoolRegistrationService = schoolRegistrationService;
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
        user.setSchoolName("VidyaSetu Demo School");

        userRepository.save(user);

        return "User registered successfully";
    }

    @PostMapping("/login")
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

        return new AuthResponse(
                token,
                user.getId(),
                user.getSchoolId(),
                schoolCode,
                user.getTeacherId(),
                user.getTeacherName(),
                user.getDisplayName(),
                user.getSchoolName(),
                SecurityAccess.normalizeRole(user.getRole()),
                Boolean.TRUE.equals(user.getForcePasswordChange())
        );
    }

    @PostMapping("/change-password")
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

        return new AuthResponse(
                token,
                user.getId(),
                user.getSchoolId(),
                schoolCode,
                user.getTeacherId(),
                user.getTeacherName(),
                user.getDisplayName(),
                user.getSchoolName(),
                SecurityAccess.normalizeRole(user.getRole()),
                false
        );
    }
}
