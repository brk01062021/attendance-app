package com.school.attendance.dto;

import java.util.ArrayList;
import java.util.List;

public class TimetableLiveResponseDTO {
    private String batchId;
    private String role;
    private String visibilityScope;
    private Boolean published;
    private Boolean locked;
    private String message;
    private List<TimetableEntryDTO> entries = new ArrayList<>();

    public TimetableLiveResponseDTO() {}
    public TimetableLiveResponseDTO(String batchId, String role, String visibilityScope, Boolean published, Boolean locked, String message, List<TimetableEntryDTO> entries) {
        this.batchId = batchId;
        this.role = role;
        this.visibilityScope = visibilityScope;
        this.published = published;
        this.locked = locked;
        this.message = message;
        this.entries = entries == null ? new ArrayList<>() : entries;
    }
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getVisibilityScope() { return visibilityScope; }
    public void setVisibilityScope(String visibilityScope) { this.visibilityScope = visibilityScope; }
    public Boolean getPublished() { return published; }
    public void setPublished(Boolean published) { this.published = published; }
    public Boolean getLocked() { return locked; }
    public void setLocked(Boolean locked) { this.locked = locked; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<TimetableEntryDTO> getEntries() { return entries; }
    public void setEntries(List<TimetableEntryDTO> entries) { this.entries = entries == null ? new ArrayList<>() : entries; }
}
