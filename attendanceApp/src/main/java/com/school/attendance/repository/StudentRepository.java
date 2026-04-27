package com.school.attendance.repository;

import com.school.attendance.entity.Student;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findByNameAndClassNameAndSection(
            String name,
            String className,
            String section
    );
    List<Student> findByClassNameAndSection(String className, String section);
    @Query("SELECT DISTINCT s.className FROM Student s ORDER BY s.className")
    List<String> findDistinctClassNames();

    @Query("SELECT DISTINCT s.section FROM Student s WHERE s.className = :className ORDER BY s.section")
    List<String> findDistinctSectionsByClassName(String className);
}
