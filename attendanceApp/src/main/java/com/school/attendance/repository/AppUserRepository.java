package com.school.attendance.repository;

import com.school.attendance.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByUsernameAndSchoolCodeIgnoreCase(String username, String schoolCode);

    Optional<AppUser> findByTeacherId(Long teacherId);

    List<AppUser> findByRoleIgnoreCase(String role);

    List<AppUser> findByRoleIgnoreCaseAndSchoolCodeIgnoreCase(String role, String schoolCode);

    List<AppUser> findBySchoolCodeIgnoreCase(String schoolCode);

    @Query("""
            select u
            from AppUser u
            where upper(u.schoolCode) = upper(:schoolCode)
              and upper(u.role) in :roles
            """)
    List<AppUser> findProvisionedRoleUsers(
            @Param("schoolCode") String schoolCode,
            @Param("roles") Collection<String> roles
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from AppUser u
            where upper(u.schoolCode) = upper(:schoolCode)
              and upper(u.role) in :roles
            """)
    int deleteProvisionedRoleUsers(
            @Param("schoolCode") String schoolCode,
            @Param("roles") Collection<String> roles
    );
}
