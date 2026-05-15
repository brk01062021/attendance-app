package com.school.attendance.dto;

public class PrincipalRiskAlertDTO {
    private String type;
    private String title;
    private String description;
    private String severity;
    private Double score;

    public PrincipalRiskAlertDTO(String type, String title, String description, String severity, Double score) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.score = score;
    }

    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getSeverity() { return severity; }
    public Double getScore() { return score; }
}
