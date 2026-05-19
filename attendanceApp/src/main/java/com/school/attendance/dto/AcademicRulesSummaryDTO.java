package com.school.attendance.dto;

import java.util.ArrayList;
import java.util.List;

public class AcademicRulesSummaryDTO {
    private Integer totalRules;
    private Integer totalWeeklyPeriodsRequired;
    private Integer availableWeeklySlots;
    private Integer theoryPeriods;
    private Integer labPeriods;
    private Integer sportsPeriods;
    private Integer activityPeriods;
    private Boolean valid;
    private List<String> warnings = new ArrayList<>();

    public AcademicRulesSummaryDTO() {}

    public AcademicRulesSummaryDTO(Integer totalRules, Integer totalWeeklyPeriodsRequired, Integer availableWeeklySlots,
                                   Integer theoryPeriods, Integer labPeriods, Integer sportsPeriods, Integer activityPeriods,
                                   Boolean valid, List<String> warnings) {
        this.totalRules = totalRules;
        this.totalWeeklyPeriodsRequired = totalWeeklyPeriodsRequired;
        this.availableWeeklySlots = availableWeeklySlots;
        this.theoryPeriods = theoryPeriods;
        this.labPeriods = labPeriods;
        this.sportsPeriods = sportsPeriods;
        this.activityPeriods = activityPeriods;
        this.valid = valid;
        this.warnings = warnings == null ? new ArrayList<>() : warnings;
    }

    public Integer getTotalRules() { return totalRules; }
    public void setTotalRules(Integer totalRules) { this.totalRules = totalRules; }
    public Integer getTotalWeeklyPeriodsRequired() { return totalWeeklyPeriodsRequired; }
    public void setTotalWeeklyPeriodsRequired(Integer totalWeeklyPeriodsRequired) { this.totalWeeklyPeriodsRequired = totalWeeklyPeriodsRequired; }
    public Integer getAvailableWeeklySlots() { return availableWeeklySlots; }
    public void setAvailableWeeklySlots(Integer availableWeeklySlots) { this.availableWeeklySlots = availableWeeklySlots; }
    public Integer getTheoryPeriods() { return theoryPeriods; }
    public void setTheoryPeriods(Integer theoryPeriods) { this.theoryPeriods = theoryPeriods; }
    public Integer getLabPeriods() { return labPeriods; }
    public void setLabPeriods(Integer labPeriods) { this.labPeriods = labPeriods; }
    public Integer getSportsPeriods() { return sportsPeriods; }
    public void setSportsPeriods(Integer sportsPeriods) { this.sportsPeriods = sportsPeriods; }
    public Integer getActivityPeriods() { return activityPeriods; }
    public void setActivityPeriods(Integer activityPeriods) { this.activityPeriods = activityPeriods; }
    public Boolean getValid() { return valid; }
    public void setValid(Boolean valid) { this.valid = valid; }
    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings == null ? new ArrayList<>() : warnings; }
}
