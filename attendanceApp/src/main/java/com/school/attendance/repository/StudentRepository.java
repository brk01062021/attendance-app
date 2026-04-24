package com.school.attendance.repository;

import com.school.attendance.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findByNameAndClassNameAndSection(
            String name,
            String className,
            String section
    );
    List<Student> findByClassNameAndSection(String className, String section);
}
