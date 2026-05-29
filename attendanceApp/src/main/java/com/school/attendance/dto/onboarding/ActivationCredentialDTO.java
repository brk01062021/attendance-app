package com.school.attendance.dto.onboarding;

public class ActivationCredentialDTO {
    private String role;
    private String username;
    private String initialPassword;
    private String displayName;
    private boolean created;

    public ActivationCredentialDTO() {}

    public ActivationCredentialDTO(String role, String username, String initialPassword, String displayName, boolean created) {
        this.role = role;
        this.username = username;
        this.initialPassword = initialPassword;
        this.displayName = displayName;
        this.created = created;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getInitialPassword() { return initialPassword; }
    public void setInitialPassword(String initialPassword) { this.initialPassword = initialPassword; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public boolean isCreated() { return created; }
    public void setCreated(boolean created) { this.created = created; }
}
