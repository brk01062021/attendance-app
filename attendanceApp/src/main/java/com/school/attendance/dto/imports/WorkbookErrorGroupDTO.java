package com.school.attendance.dto.imports;

import java.util.ArrayList;
import java.util.List;

public class WorkbookErrorGroupDTO {
    private String category;
    private String title;
    private String explanation;
    private String recommendedAction;
    private int errorCount;
    private int warningCount;
    private List<ImportValidationIssueDTO> issues = new ArrayList<>();

    public WorkbookErrorGroupDTO() { }

    public WorkbookErrorGroupDTO(String category, String title, String explanation, String recommendedAction) {
        this.category = category;
        this.title = title;
        this.explanation = explanation;
        this.recommendedAction = recommendedAction;
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }
    public int getErrorCount() { return errorCount; }
    public void setErrorCount(int errorCount) { this.errorCount = errorCount; }
    public int getWarningCount() { return warningCount; }
    public void setWarningCount(int warningCount) { this.warningCount = warningCount; }
    public List<ImportValidationIssueDTO> getIssues() { return issues; }
    public void setIssues(List<ImportValidationIssueDTO> issues) { this.issues = issues; }
}
