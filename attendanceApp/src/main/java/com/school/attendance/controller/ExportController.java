package com.school.attendance.controller;

import com.school.attendance.dto.TeacherReplacementLoadDTO;
import com.school.attendance.service.TeacherWorkloadService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/export")
@CrossOrigin(origins = "*")
public class ExportController {

    private final TeacherWorkloadService teacherWorkloadService;

    public ExportController(TeacherWorkloadService teacherWorkloadService) {
        this.teacherWorkloadService = teacherWorkloadService;
    }

    @GetMapping(value = "/teacher-workload.csv", produces = "text/csv")
    public ResponseEntity<String> exportTeacherWorkloadCsv(
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate
    ) {
        List<TeacherReplacementLoadDTO> rows = teacherWorkloadService.getReplacementLoad(fromDate, toDate);
        StringBuilder csv = new StringBuilder("Teacher Id,Teacher Name,Scheduled Periods,Replacement Periods,Leave Periods,Overload Score,Risk Level\n");
        for (TeacherReplacementLoadDTO row : rows) {
            csv.append(row.getTeacherId()).append(',')
                    .append(escape(row.getTeacherName())).append(',')
                    .append(row.getScheduledPeriods()).append(',')
                    .append(row.getReplacementPeriods()).append(',')
                    .append(row.getLeavePeriods()).append(',')
                    .append(row.getOverloadScore()).append(',')
                    .append(row.getRiskLevel()).append('\n');
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=teacher-workload.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.toString());
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
