package com.school.attendance.dto;

public class AcademicRuleDTO {
    private String ruleId;
    private String className;
    private String subjectName;
    private String subjectType;
    private Integer weeklyPeriods;
    private Boolean fixedPeriodRequired;
    private Integer preferredPeriodNumber;
    private Boolean sameTeacherContinuityRequired;
    private String priority;

    public AcademicRuleDTO() {}

    public AcademicRuleDTO(String ruleId, String className, String subjectName, String subjectType, Integer weeklyPeriods,
                           Boolean fixedPeriodRequired, Integer preferredPeriodNumber, Boolean sameTeacherContinuityRequired, String priority) {
        this.ruleId = ruleId;
        this.className = className;
        this.subjectName = subjectName;
        this.subjectType = subjectType;
        this.weeklyPeriods = weeklyPeriods;
        this.fixedPeriodRequired = fixedPeriodRequired;
        this.preferredPeriodNumber = preferredPeriodNumber;
        this.sameTeacherContinuityRequired = sameTeacherContinuityRequired;
        this.priority = priority;
    }

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getSubjectType() { return subjectType; }
    public void setSubjectType(String subjectType) { this.subjectType = subjectType; }
    public Integer getWeeklyPeriods() { return weeklyPeriods; }
    public void setWeeklyPeriods(Integer weeklyPeriods) { this.weeklyPeriods = weeklyPeriods; }
    public Boolean getFixedPeriodRequired() { return fixedPeriodRequired; }
    public void setFixedPeriodRequired(Boolean fixedPeriodRequired) { this.fixedPeriodRequired = fixedPeriodRequired; }
    public Integer getPreferredPeriodNumber() { return preferredPeriodNumber; }
    public void setPreferredPeriodNumber(Integer preferredPeriodNumber) { this.preferredPeriodNumber = preferredPeriodNumber; }
    public Boolean getSameTeacherContinuityRequired() { return sameTeacherContinuityRequired; }
    public void setSameTeacherContinuityRequired(Boolean sameTeacherContinuityRequired) { this.sameTeacherContinuityRequired = sameTeacherContinuityRequired; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}
