package com.school.attendance.repository;

import com.school.attendance.entity.TeacherAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TeacherAssignmentRepository extends JpaRepository<TeacherAssignment, Long> {

    @Query("SELECT DISTINCT t.subjectName FROM TeacherAssignment t WHERE t.teacherId = :teacherId ORDER BY t.subjectName")
    List<String> findSubjectsByTeacherId(Long teacherId);

    @Query("SELECT DISTINCT t.className FROM TeacherAssignment t WHERE t.teacherId = :teacherId AND t.subjectName = :subjectName ORDER BY t.className")
    List<String> findClassesByTeacherIdAndSubjectName(Long teacherId, String subjectName);

    @Query("SELECT DISTINCT t.section FROM TeacherAssignment t WHERE t.teacherId = :teacherId AND t.subjectName = :subjectName AND t.className = :className ORDER BY t.section")
    List<String> findSectionsByTeacherIdSubjectNameAndClassName(Long teacherId, String subjectName, String className);
}