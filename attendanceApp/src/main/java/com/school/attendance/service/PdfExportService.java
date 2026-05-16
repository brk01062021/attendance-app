package com.school.attendance.service;

import org.springframework.stereotype.Service;

@Service
public class PdfExportService {
    public String buildPlaceholderMessage() {
        return "PDF export foundation is ready. Add PDF library wiring when production report templates are finalized.";
    }
}
