package com.school.attendance.repository;

import com.school.attendance.entity.TeacherExamResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeacherExamResultRepository extends JpaRepository<TeacherExamResult, Long> {
    Optional<TeacherExamResult> findBySchoolIdIgnoreCaseAndTeacherIdAndClassNameIgnoreCaseAndSectionIgnoreCaseAndSubjectNameIgnoreCaseAndExamNameIgnoreCaseAndStudentId(
            String schoolId,
            Long teacherId,
            String className,
            String section,
            String subjectName,
            String examName,
            Long studentId
    );

    List<TeacherExamResult> findBySchoolIdIgnoreCaseAndTeacherIdOrderBySavedAtDesc(String schoolId, Long teacherId);
}
