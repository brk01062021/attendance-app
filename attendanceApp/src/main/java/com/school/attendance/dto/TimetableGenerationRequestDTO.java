package com.school.attendance.dto;

import java.util.ArrayList;
import java.util.List;

public class TimetableGenerationRequestDTO {
    private String academicYear;
    private String generationMode;
    private List<String> classNames = new ArrayList<>();
    private List<String> sections = new ArrayList<>();
    private List<Long> teacherIds = new ArrayList<>();
    private String teacherPoolSource;
    private Boolean autoLoadSectionsEnabled;
    private Boolean autoDefaultTeacherPoolEnabled;
    private List<ClassTeacherPoolDTO> selectedTeacherPools = new ArrayList<>();
    private Boolean equalDistributionEnabled;
    private Boolean workloadBalancingEnabled;
    private Boolean fixedLabPeriodsEnabled;
    private Boolean avoidTeacherGapsEnabled;
    private Boolean sameTeacherContinuityEnabled;
    private Boolean preventConsecutiveLabsEnabled;
    private Boolean academicRulesEngineEnabled;
    private java.util.List<AcademicRuleDTO> academicRules = new java.util.ArrayList<>();

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public String getGenerationMode() { return generationMode; }
    public void setGenerationMode(String generationMode) { this.generationMode = generationMode; }
    public List<String> getClassNames() { return classNames; }
    public void setClassNames(List<String> classNames) { this.classNames = classNames == null ? new ArrayList<>() : classNames; }
    public List<String> getSections() { return sections; }
    public void setSections(List<String> sections) { this.sections = sections == null ? new ArrayList<>() : sections; }
    public List<Long> getTeacherIds() { return teacherIds; }
    public void setTeacherIds(List<Long> teacherIds) { this.teacherIds = teacherIds == null ? new ArrayList<>() : teacherIds; }
    public String getTeacherPoolSource() { return teacherPoolSource; }
    public void setTeacherPoolSource(String teacherPoolSource) { this.teacherPoolSource = teacherPoolSource; }
    public Boolean getAutoLoadSectionsEnabled() { return autoLoadSectionsEnabled; }
    public void setAutoLoadSectionsEnabled(Boolean autoLoadSectionsEnabled) { this.autoLoadSectionsEnabled = autoLoadSectionsEnabled; }
    public Boolean getAutoDefaultTeacherPoolEnabled() { return autoDefaultTeacherPoolEnabled; }
    public void setAutoDefaultTeacherPoolEnabled(Boolean autoDefaultTeacherPoolEnabled) { this.autoDefaultTeacherPoolEnabled = autoDefaultTeacherPoolEnabled; }
    public List<ClassTeacherPoolDTO> getSelectedTeacherPools() { return selectedTeacherPools; }
    public void setSelectedTeacherPools(List<ClassTeacherPoolDTO> selectedTeacherPools) { this.selectedTeacherPools = selectedTeacherPools == null ? new ArrayList<>() : selectedTeacherPools; }
    public Boolean getEqualDistributionEnabled() { return equalDistributionEnabled; }
    public void setEqualDistributionEnabled(Boolean equalDistributionEnabled) { this.equalDistributionEnabled = equalDistributionEnabled; }
    public Boolean getWorkloadBalancingEnabled() { return workloadBalancingEnabled; }
    public void setWorkloadBalancingEnabled(Boolean workloadBalancingEnabled) { this.workloadBalancingEnabled = workloadBalancingEnabled; }
    public Boolean getFixedLabPeriodsEnabled() { return fixedLabPeriodsEnabled; }
    public void setFixedLabPeriodsEnabled(Boolean fixedLabPeriodsEnabled) { this.fixedLabPeriodsEnabled = fixedLabPeriodsEnabled; }
    public Boolean getAvoidTeacherGapsEnabled() { return avoidTeacherGapsEnabled; }
    public void setAvoidTeacherGapsEnabled(Boolean avoidTeacherGapsEnabled) { this.avoidTeacherGapsEnabled = avoidTeacherGapsEnabled; }
    public Boolean getSameTeacherContinuityEnabled() { return sameTeacherContinuityEnabled; }
    public void setSameTeacherContinuityEnabled(Boolean sameTeacherContinuityEnabled) { this.sameTeacherContinuityEnabled = sameTeacherContinuityEnabled; }
    public Boolean getPreventConsecutiveLabsEnabled() { return preventConsecutiveLabsEnabled; }
    public void setPreventConsecutiveLabsEnabled(Boolean preventConsecutiveLabsEnabled) { this.preventConsecutiveLabsEnabled = preventConsecutiveLabsEnabled; }
    public Boolean getAcademicRulesEngineEnabled() { return academicRulesEngineEnabled; }
    public void setAcademicRulesEngineEnabled(Boolean academicRulesEngineEnabled) { this.academicRulesEngineEnabled = academicRulesEngineEnabled; }
    public java.util.List<AcademicRuleDTO> getAcademicRules() { return academicRules; }
    public void setAcademicRules(java.util.List<AcademicRuleDTO> academicRules) { this.academicRules = academicRules == null ? new java.util.ArrayList<>() : academicRules; }
}
