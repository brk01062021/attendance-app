package com.school.attendance.dto;

import java.util.ArrayList;
import java.util.List;

public class TimetableClassSectionReviewDTO {
    private String className;
    private String section;
    private String label;
    private Integer totalPeriods;
    private Integer conflictCount;
    private List<TimetableEntryDTO> entries = new ArrayList<>();

    public TimetableClassSectionReviewDTO() {
    }

    public TimetableClassSectionReviewDTO(String className, String section, String label, Integer totalPeriods,
                                          Integer conflictCount, List<TimetableEntryDTO> entries) {
        this.className = className;
        this.section = section;
        this.label = label;
        this.totalPeriods = totalPeriods;
        this.conflictCount = conflictCount;
        this.entries = entries == null ? new ArrayList<>() : entries;
    }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public Integer getTotalPeriods() { return totalPeriods; }
    public void setTotalPeriods(Integer totalPeriods) { this.totalPeriods = totalPeriods; }
    public Integer getConflictCount() { return conflictCount; }
    public void setConflictCount(Integer conflictCount) { this.conflictCount = conflictCount; }
    public List<TimetableEntryDTO> getEntries() { return entries; }
    public void setEntries(List<TimetableEntryDTO> entries) { this.entries = entries == null ? new ArrayList<>() : entries; }
}
