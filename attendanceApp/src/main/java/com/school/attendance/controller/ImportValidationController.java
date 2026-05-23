package com.school.attendance.controller;

import com.school.attendance.common.dto.ApiResponse;
import com.school.attendance.dto.imports.ImportPreviewResponseDTO;
import com.school.attendance.dto.imports.ImportValidationRequestDTO;
import com.school.attendance.service.ImportValidationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/imports")
public class ImportValidationController {

    private final ImportValidationService importValidationService;

    public ImportValidationController(ImportValidationService importValidationService) {
        this.importValidationService = importValidationService;
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.success("Import validation engine is ready", Map.of(
                "service", "excel-import-validation",
                "status", "UP",
                "timestamp", Instant.now().toString()
        ));
    }

    @PostMapping("/preview/validate")
    public ApiResponse<ImportPreviewResponseDTO> validatePreview(@RequestBody ImportValidationRequestDTO request) {
        ImportPreviewResponseDTO response = importValidationService.validatePreview(request);
        return ApiResponse.success("Excel import preview validation completed", response);
    }
}
