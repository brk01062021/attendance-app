package com.school.attendance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.attendance.dto.LoginRequest;
import com.school.attendance.entity.AppUser;
import com.school.attendance.repository.AppUserRepository;
import com.school.attendance.repository.SchoolImportStagingRecordRepository;
import com.school.attendance.repository.SchoolImportUploadRepository;
import com.school.attendance.security.JwtUtil;
import com.school.attendance.service.onboarding.SchoolRegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AuthControllerTenantBindingTest {

    @Test
    void loginRejectsWhenUsernameExistsInDifferentTenantOnly() {
        AppUserRepository repository = mock(AppUserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        SchoolRegistrationService onboarding = mock(SchoolRegistrationService.class);
        SchoolImportUploadRepository schoolImportUploadRepository = mock(SchoolImportUploadRepository.class);
        SchoolImportStagingRecordRepository schoolImportStagingRecordRepository = mock(SchoolImportStagingRecordRepository.class);
        ObjectMapper objectMapper = new ObjectMapper();
        AuthController controller = new AuthController(
                repository,
                encoder,
                jwtUtil,
                onboarding,
                schoolImportUploadRepository,
                schoolImportStagingRecordRepository,
                objectMapper
        );

        LoginRequest request = new LoginRequest();
        request.setUsername("brk1.admin");
        request.setPassword("password");
        request.setSchoolId("TST2");

        when(onboarding.isLoginEnabledForSchoolId("TST2")).thenReturn(true);
        when(repository.findByUsernameAndSchoolCodeIgnoreCase("brk1.admin", "TST2")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> controller.login(request));
        assertEquals("Invalid username, password, or school ID", ex.getMessage());
        verify(repository, never()).findByUsername("brk1.admin");
        verifyNoInteractions(encoder);
    }

    @Test
    void loginAcceptsOnlyWhenUsernamePasswordAndSchoolIdMatchSameTenant() {
        AppUserRepository repository = mock(AppUserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        SchoolRegistrationService onboarding = mock(SchoolRegistrationService.class);
        SchoolImportUploadRepository schoolImportUploadRepository = mock(SchoolImportUploadRepository.class);
        SchoolImportStagingRecordRepository schoolImportStagingRecordRepository = mock(SchoolImportStagingRecordRepository.class);
        ObjectMapper objectMapper = new ObjectMapper();
        AuthController controller = new AuthController(
                repository,
                encoder,
                jwtUtil,
                onboarding,
                schoolImportUploadRepository,
                schoolImportStagingRecordRepository,
                objectMapper
        );

        AppUser user = new AppUser();
        user.setUsername("tst2.admin");
        user.setPassword("encoded");
        user.setRole("ADMIN");
        user.setSchoolCode("TST2");
        user.setSchoolName("Second Test School");
        user.setDisplayName("TST2 Admin");
        user.setCredentialsActive(true);
        user.setForcePasswordChange(true);

        LoginRequest request = new LoginRequest();
        request.setUsername("tst2.admin");
        request.setPassword("password");
        request.setSchoolId("TST2");

        when(onboarding.isLoginEnabledForSchoolId("TST2")).thenReturn(true);
        when(repository.findByUsernameAndSchoolCodeIgnoreCase("tst2.admin", "TST2")).thenReturn(Optional.of(user));
        when(encoder.matches("password", "encoded")).thenReturn(true);
        when(jwtUtil.generateToken(eq("tst2.admin"), eq("ADMIN"), eq("TST2"), any())).thenReturn("jwt");

        var response = controller.login(request);

        assertEquals("TST2", response.getSchoolCode());
        assertEquals("ADMIN", response.getRole());
        assertTrue(response.isForcePasswordChange());
        assertTrue(response.isCredentialsActive());
    }
}
