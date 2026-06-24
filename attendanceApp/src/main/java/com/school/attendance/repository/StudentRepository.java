package com.school.attendance.repository;

import com.school.attendance.dto.StudentSearchDTO;
import com.school.attendance.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findFirstByAdmissionNumberIgnoreCase(String admissionNumber);

    Optional<Student> findFirstByRollNumberIgnoreCase(String rollNumber);

    List<Student> findByNameAndClassNameAndSection(
            String name,
            String className,
            String section
    );

    List<Student> findByClassNameAndSection(
            String className,
            String section
    );

    @Query("""
            SELECT s
            FROM Student s
            WHERE LOWER(REPLACE(REPLACE(COALESCE(s.className, ''), 'Class ', ''), 'class ', '')) =
                  LOWER(REPLACE(REPLACE(COALESCE(:className, ''), 'Class ', ''), 'class ', ''))
              AND LOWER(TRIM(COALESCE(s.section, ''))) = LOWER(TRIM(COALESCE(:section, '')))
            ORDER BY s.rollNumber ASC, s.name ASC
            """)
    List<Student> findByClassAndSectionFlexible(
            @Param("className") String className,
            @Param("section") String section
    );

    @Query("SELECT DISTINCT s.className FROM Student s ORDER BY s.className")
    List<String> findDistinctClassNames();

    @Query("SELECT DISTINCT s.section FROM Student s WHERE s.className = :className ORDER BY s.section")
    List<String> findDistinctSectionsByClassName(
            @Param("className") String className
    );

    List<Student> findAllByOrderByClassNameAscSectionAscNameAsc();

    List<Student> findByClassNameOrderBySectionAscNameAsc(String className);

    List<Student> findByClassNameAndSectionOrderByNameAsc(
            String className,
            String section
    );

    @Query("""
            SELECT new com.school.attendance.dto.StudentSearchDTO(
                s.id,
                s.name,
                s.admissionNumber,
                s.rollNumber,
                s.className,
                s.section
            )
            FROM Student s
            WHERE LOWER(s.className) = LOWER(:className)
              AND LOWER(s.section) = LOWER(:section)
              AND (
                    :query IS NULL
                    OR :query = ''
                    OR LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(s.admissionNumber, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(s.rollNumber, '')) LIKE LOWER(CONCAT('%', :query, '%'))
              )
            ORDER BY s.name ASC
            """)
    List<StudentSearchDTO> searchStudentsForReport(
            @Param("className") String className,
            @Param("section") String section,
            @Param("query") String query
    );

    @Query("""
            SELECT new com.school.attendance.dto.StudentSearchDTO(
                s.id,
                s.name,
                s.admissionNumber,
                s.rollNumber,
                s.className,
                s.section
            )
            FROM Student s
            WHERE (:className IS NULL OR :className = '' OR LOWER(s.className) = LOWER(:className))
              AND (:section IS NULL OR :section = '' OR LOWER(s.section) = LOWER(:section))
              AND (
                    :query IS NULL
                    OR :query = ''
                    OR LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(s.admissionNumber, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(s.rollNumber, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(s.className, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(s.section, '')) LIKE LOWER(CONCAT('%', :query, '%'))
              )
            ORDER BY s.className ASC, s.section ASC, s.name ASC
            """)
    List<StudentSearchDTO> searchTenantStudents(@Param("query") String query,
                                                @Param("className") String className,
                                                @Param("section") String section);

    @Query("""
            SELECT DISTINCT CONCAT(s.className, ' - ', s.section)
            FROM Student s
            WHERE s.className IS NOT NULL
              AND s.section IS NOT NULL
            ORDER BY CONCAT(s.className, ' - ', s.section) ASC
            """)
    List<String> findDistinctClassSectionLabels();

}
