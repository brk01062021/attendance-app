package com.school.attendance.controller;

import com.school.attendance.entity.AppUser;
import com.school.attendance.repository.AppUserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dev/users")
@CrossOrigin(origins = "*")
public class DevUserController {

    private final AppUserRepository appUserRepository;

    public DevUserController(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @PostMapping("/principal")
    public ResponseEntity<AppUser> createPrincipalUser() {
        AppUser user = new AppUser();
        user.setUsername("principal");
        user.setPassword("principal123");
        user.setRole("PRINCIPAL");
        user.setTeacherId(null);
        user.setTeacherName("Principal User");

        AppUser saved = appUserRepository.save(user);
        return ResponseEntity.ok(saved);
    }
}