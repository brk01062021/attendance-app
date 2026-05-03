package com.school.attendance.controller;

import com.school.attendance.dto.SchoolBrandingDTO;
import com.school.attendance.entity.SchoolBranding;
import com.school.attendance.repository.SchoolBrandingRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/school-branding")
@CrossOrigin(origins = "*")
public class SchoolBrandingController {

    private final SchoolBrandingRepository schoolBrandingRepository;

    public SchoolBrandingController(SchoolBrandingRepository schoolBrandingRepository) {
        this.schoolBrandingRepository = schoolBrandingRepository;
    }

    @GetMapping
    public List<SchoolBranding> getAllSchoolBranding() {
        return schoolBrandingRepository.findAll();
    }

    @PostMapping
    public SchoolBranding createSchoolBranding(@RequestBody SchoolBrandingDTO dto) {
        SchoolBranding branding = new SchoolBranding();
        mapDtoToEntity(dto, branding);
        return schoolBrandingRepository.save(branding);
    }

    @PutMapping("/{id}")
    public SchoolBranding updateSchoolBranding(
            @PathVariable Long id,
            @RequestBody SchoolBrandingDTO dto
    ) {
        SchoolBranding branding = schoolBrandingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("School branding not found with id: " + id));

        mapDtoToEntity(dto, branding);
        return schoolBrandingRepository.save(branding);
    }

    private void mapDtoToEntity(SchoolBrandingDTO dto, SchoolBranding branding) {
        branding.setSchoolName(dto.getSchoolName());
        branding.setShortName(dto.getShortName());
        branding.setLogoUrl(dto.getLogoUrl());
        branding.setPrimaryColor(dto.getPrimaryColor());
        branding.setSecondaryColor(dto.getSecondaryColor());
        branding.setWatermarkText(dto.getWatermarkText());
        branding.setTagline(dto.getTagline());
        branding.setAddress(dto.getAddress());
        branding.setPhone(dto.getPhone());
        branding.setEmail(dto.getEmail());
        branding.setWebsite(dto.getWebsite());
        branding.setPrincipalName(dto.getPrincipalName());
    }
}