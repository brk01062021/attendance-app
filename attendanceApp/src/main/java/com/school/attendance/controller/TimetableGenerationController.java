package com.school.attendance.controller;

import com.school.attendance.dto.ClassTeacherPoolDTO;
import com.school.attendance.dto.TimetableGenerationRequestDTO;
import com.school.attendance.dto.TimetableGenerationResponseDTO;
import com.school.attendance.service.TimetableGenerationService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/timetable")
@CrossOrigin(origins = "*")
public class TimetableGenerationController {

    private final TimetableGenerationService timetableGenerationService;

    public TimetableGenerationController(TimetableGenerationService timetableGenerationService) {
        this.timetableGenerationService = timetableGenerationService;
    }

    @PostMapping("/generate")
    public TimetableGenerationResponseDTO generate(@RequestBody TimetableGenerationRequestDTO request) {
        return timetableGenerationService.generate(request);
    }

    @GetMapping("/default-pools")
    public List<ClassTeacherPoolDTO> getDefaultPools() {
        return timetableGenerationService.getDefaultPools();
    }

    @PostMapping("/validate")
    public TimetableGenerationResponseDTO validate(@RequestBody TimetableGenerationRequestDTO request) {
        return timetableGenerationService.validate(request);
    }
}
