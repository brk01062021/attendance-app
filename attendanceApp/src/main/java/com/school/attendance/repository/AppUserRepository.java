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

    default Optional<AppUser> findByUsernameAndSchoolCodeIgnoreCase(String username, String schoolCode) {
        List<AppUser> matches = findAllByUsernameAndSchoolCodeIgnoreCase(username, schoolCode);
        return matches.isEmpty() ? Optional.empty() : Optional.of(matches.get(0));
    }

    @Query("""
            select u
            from AppUser u
            where upper(u.username) = upper(:username)
              and upper(u.schoolCode) = upper(:schoolCode)
            order by u.credentialsActive desc, u.id desc
            """)
    List<AppUser> findAllByUsernameAndSchoolCodeIgnoreCase(@Param("username") String username,
                                                           @Param("schoolCode") String schoolCode);

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
