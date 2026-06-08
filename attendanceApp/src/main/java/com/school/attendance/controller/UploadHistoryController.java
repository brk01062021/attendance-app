package com.school.attendance.controller;

import com.school.attendance.dto.UploadedFileHistoryDTO;
import com.school.attendance.service.UploadedFileMetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class UploadHistoryController {

    private final UploadedFileMetadataService uploadedFileMetadataService;

    @GetMapping("/history")
    public ResponseEntity<List<UploadedFileHistoryDTO>> history(
            @RequestParam String schoolId,
            @RequestParam(required = false) String module
    ) {

        List<UploadedFileHistoryDTO> history =
                uploadedFileMetadataService.history(
                        schoolId.trim().toUpperCase(),
                        module
                );

        return ResponseEntity.ok(history);
    }
}