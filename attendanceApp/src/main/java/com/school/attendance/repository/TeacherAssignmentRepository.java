package com.school.attendance.repository;

import com.school.attendance.dto.ReplacementTeacherDTO;
import com.school.attendance.dto.TeacherSearchDTO;
import com.school.attendance.entity.TeacherAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeacherAssignmentRepository extends JpaRepository<TeacherAssignment, Long> {

    @Query("SELECT DISTINCT t.subjectName FROM TeacherAssignment t WHERE t.teacherId = :teacherId ORDER BY t.subjectName")
    List<String> findSubjectsByTeacherId(Long teacherId);

    @Query("SELECT DISTINCT t.className FROM TeacherAssignment t WHERE t.teacherId = :teacherId AND t.subjectName = :subjectName ORDER BY t.className")
    List<String> findClassesByTeacherIdAndSubjectName(Long teacherId, String subjectName);

    @Query("SELECT DISTINCT t.section FROM TeacherAssignment t WHERE t.teacherId = :teacherId AND t.subjectName = :subjectName AND t.className = :className ORDER BY t.section")
    List<String> findSectionsByTeacherIdSubjectNameAndClassName(Long teacherId, String subjectName, String className);

    // Teacher Insight Report - search teachers by name or id text.
    @Query("""
            SELECT DISTINCT new com.school.attendance.dto.TeacherSearchDTO(
                t.teacherId,
                t.teacherName
            )
            FROM TeacherAssignment t
            WHERE :keyword IS NULL
               OR :keyword = ''
               OR LOWER(t.teacherName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR STR(t.teacherId) LIKE CONCAT('%', :keyword, '%')
            ORDER BY t.teacherName ASC
            """)
    List<TeacherSearchDTO> searchTeachers(@Param("keyword") String keyword);

    List<TeacherAssignment> findByTeacherIdOrderByClassNameAscSectionAscSubjectNameAsc(Long teacherId);

    @Query("""
                SELECT new com.school.attendance.dto.ReplacementTeacherDTO(
                    t.teacherId,
                    t.teacherName,
                    t.className,
                    t.section,
                    t.subjectName,
                    ''
                )
                FROM TeacherAssignment t
                WHERE t.teacherId <> :absentTeacherId
                ORDER BY t.className DESC, t.section ASC, t.teacherName ASC
            """)
    List<ReplacementTeacherDTO> findAllPossibleReplacementTeachers(Long absentTeacherId);

    @Query("""
                SELECT new com.school.attendance.dto.ReplacementTeacherDTO(
                    t.teacherId,
                    t.teacherName,
                    t.className,
                    t.section,
                    t.subjectName,
                    'Best Match'
                )
                FROM TeacherAssignment t
                WHERE t.teacherId <> :absentTeacherId
                AND t.className = :className
                AND t.section = :section
                AND t.subjectName = :subjectName
                ORDER BY t.teacherName ASC
            """)
    List<ReplacementTeacherDTO> findBestMatchReplacementTeachers(
            Long absentTeacherId,
            String className,
            String section,
            String subjectName
    );

    @Query("""
                SELECT new com.school.attendance.dto.ReplacementTeacherDTO(
                    t.teacherId,
                    t.teacherName,
                    t.className,
                    t.section,
                    t.subjectName,
                    'Same Class'
                )
                FROM TeacherAssignment t
                WHERE t.teacherId <> :absentTeacherId
                AND t.className = :className
                AND t.section = :section
                ORDER BY t.teacherName ASC
            """)
    List<ReplacementTeacherDTO> findSameClassReplacementTeachers(
            Long absentTeacherId,
            String className,
            String section
    );

    @Query("""
                SELECT new com.school.attendance.dto.ReplacementTeacherDTO(
                    t.teacherId,
                    t.teacherName,
                    t.className,
                    t.section,
                    t.subjectName,
                    'Other'
                )
                FROM TeacherAssignment t
                WHERE t.teacherId <> :absentTeacherId
                ORDER BY t.teacherName ASC
            """)
    List<ReplacementTeacherDTO> findOtherReplacementTeachers(
            Long absentTeacherId
    );
}
