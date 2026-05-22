package com.school.attendance.controller;

import com.school.attendance.dto.AuthResponse;
import com.school.attendance.dto.LoginRequest;
import com.school.attendance.dto.RegisterRequest;
import com.school.attendance.entity.AppUser;
import com.school.attendance.repository.AppUserRepository;
import com.school.attendance.security.JwtUtil;
import com.school.attendance.security.SecurityAccess;
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

    public AuthController(AppUserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
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

        AppUser user = userRepository.findByUsernameAndSchoolCodeIgnoreCase(request.getUsername(), requestedSchoolCode)
                .or(() -> userRepository.findByUsername(request.getUsername()))
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
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
                SecurityAccess.normalizeRole(user.getRole())
        );
    }
}
