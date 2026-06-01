package com.school.attendance.dto.imports;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class WorkbookErrorIntelligenceDTO {
    private String schoolId;
    private String fileName;
    private String status;
    private String headline;
    private int totalErrors;
    private int totalWarnings;
    private boolean activationBlocked;
    private List<String> missingSheets = new ArrayList<>();
    private List<String> schoolIdMismatchExplanations = new ArrayList<>();
    private List<WorkbookErrorGroupDTO> groups = new ArrayList<>();
    private List<String> activationBlockers = new ArrayList<>();
    private Instant generatedAt = Instant.now();

    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }
    public int getTotalErrors() { return totalErrors; }
    public void setTotalErrors(int totalErrors) { this.totalErrors = totalErrors; }
    public int getTotalWarnings() { return totalWarnings; }
    public void setTotalWarnings(int totalWarnings) { this.totalWarnings = totalWarnings; }
    public boolean isActivationBlocked() { return activationBlocked; }
    public void setActivationBlocked(boolean activationBlocked) { this.activationBlocked = activationBlocked; }
    public List<String> getMissingSheets() { return missingSheets; }
    public void setMissingSheets(List<String> missingSheets) { this.missingSheets = missingSheets; }
    public List<String> getSchoolIdMismatchExplanations() { return schoolIdMismatchExplanations; }
    public void setSchoolIdMismatchExplanations(List<String> schoolIdMismatchExplanations) { this.schoolIdMismatchExplanations = schoolIdMismatchExplanations; }
    public List<WorkbookErrorGroupDTO> getGroups() { return groups; }
    public void setGroups(List<WorkbookErrorGroupDTO> groups) { this.groups = groups; }
    public List<String> getActivationBlockers() { return activationBlockers; }
    public void setActivationBlockers(List<String> activationBlockers) { this.activationBlockers = activationBlockers; }
    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }
}
