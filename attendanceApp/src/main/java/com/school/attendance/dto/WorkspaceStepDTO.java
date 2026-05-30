package com.school.attendance.dto;

public class WorkspaceStepDTO {
    private String key;
    private String label;
    private boolean completed;
    private boolean requiredBeforeImport;

    public WorkspaceStepDTO() { }
    public WorkspaceStepDTO(String key, String label, boolean completed, boolean requiredBeforeImport) {
        this.key = key;
        this.label = label;
        this.completed = completed;
        this.requiredBeforeImport = requiredBeforeImport;
    }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public boolean isRequiredBeforeImport() { return requiredBeforeImport; }
    public void setRequiredBeforeImport(boolean requiredBeforeImport) { this.requiredBeforeImport = requiredBeforeImport; }
}
