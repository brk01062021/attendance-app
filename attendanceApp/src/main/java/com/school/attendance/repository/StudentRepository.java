package com.school.attendance.repository;

import com.school.attendance.dto.StudentSearchDTO;
import com.school.attendance.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findByNameAndClassNameAndSection(
            String name,
            String className,
            String section
    );

    List<Student> findByClassNameAndSection(
            String className,
            String section
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
}
