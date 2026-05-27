package com.school.attendance.service;

import com.school.attendance.dto.TeacherSearchDTO;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Sprint 3 operational lookup service.
 *
 * These lookup APIs are used by the web onboarding/import screens before a school has
 * completed real data import. They must therefore be safe even when DB lookup tables
 * are empty or not yet migrated. Real repository-backed lookups can be reintroduced
 * incrementally after the import commit workflow starts persisting tenant data.
 */
@Service
public class OperationalLookupService {

    public List<String> academicYears() {
        int current = Year.now().getValue();
        List<String> years = new ArrayList<>();
        years.add((current - 1) + "-" + current);
        years.add(current + "-" + (current + 1));
        years.add((current + 1) + "-" + (current + 2));
        return years;
    }

    public List<String> classes(String schoolId) {
        List<String> values = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            values.add("Class " + i);
        }
        return values;
    }

    public List<String> sections(String schoolId, String className) {
        return List.of("A", "B");
    }

    public List<String> subjects(String schoolId) {
        return List.of(
                "English",
                "Telugu",
                "Hindi",
                "Mathematics",
                "Science",
                "Social",
                "Computers",
                "Sports",
                "G.K"
        );
    }

    public List<TeacherSearchDTO> teachers(String schoolId, String query) {
        List<TeacherSearchDTO> defaults = List.of(
                new TeacherSearchDTO(1L, "Rakshanda"),
                new TeacherSearchDTO(2L, "Ramesh"),
                new TeacherSearchDTO(3L, "Sravani"),
                new TeacherSearchDTO(4L, "Anil"),
                new TeacherSearchDTO(5L, "Priya")
        );

        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (q.isBlank()) {
            return defaults;
        }

        return defaults.stream()
                .filter(t -> t.getTeacherName() != null && t.getTeacherName().toLowerCase(Locale.ROOT).contains(q))
                .toList();
    }
}
