package com.school.attendance.service;

import org.springframework.stereotype.Service;

@Service
public class ExcelExportService {
    public String buildPlaceholderMessage() {
        return "Excel export foundation is ready. Current Day 5 export endpoint provides CSV-compatible Excel download.";
    }
}
