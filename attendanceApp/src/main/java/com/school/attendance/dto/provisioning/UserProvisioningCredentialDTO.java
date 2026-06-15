package com.school.attendance.dto.provisioning;

public class UserProvisioningCredentialDTO {
    private String role;
    private String username;
    private String temporaryPassword;
    private String displayName;
    private String linkedReference;
    private boolean created;
    private boolean updated;

    public UserProvisioningCredentialDTO() {}

    public UserProvisioningCredentialDTO(String role, String username, String temporaryPassword, String displayName, String linkedReference, boolean created, boolean updated) {
        this.role = role;
        this.username = username;
        this.temporaryPassword = temporaryPassword;
        this.displayName = displayName;
        this.linkedReference = linkedReference;
        this.created = created;
        this.updated = updated;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getTemporaryPassword() { return temporaryPassword; }
    public void setTemporaryPassword(String temporaryPassword) { this.temporaryPassword = temporaryPassword; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getLinkedReference() { return linkedReference; }
    public void setLinkedReference(String linkedReference) { this.linkedReference = linkedReference; }
    public boolean isCreated() { return created; }
    public void setCreated(boolean created) { this.created = created; }
    public boolean isUpdated() { return updated; }
    public void setUpdated(boolean updated) { this.updated = updated; }
}
