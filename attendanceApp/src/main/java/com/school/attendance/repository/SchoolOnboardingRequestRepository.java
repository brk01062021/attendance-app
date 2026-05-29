package com.school.attendance.repository;

import com.school.attendance.entity.SchoolOnboardingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolOnboardingRequestRepository extends JpaRepository<SchoolOnboardingRequest, Long> {
    Optional<SchoolOnboardingRequest> findByReferenceId(String referenceId);
    Optional<SchoolOnboardingRequest> findTopBySchoolIdOrderByUpdatedAtDesc(String schoolId);
    boolean existsBySchoolIdAndStatusIn(String schoolId, List<String> statuses);
    List<SchoolOnboardingRequest> findByStatusInOrderByUpdatedAtDesc(List<String> statuses);
}
