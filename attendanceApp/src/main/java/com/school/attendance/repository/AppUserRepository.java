package com.school.attendance.repository;

import com.school.attendance.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByUsernameAndSchoolCodeIgnoreCase(String username, String schoolCode);

    Optional<AppUser> findByTeacherId(Long teacherId);

    List<AppUser> findByRoleIgnoreCase(String role);

    List<AppUser> findBySchoolCodeIgnoreCase(String schoolCode);
}
