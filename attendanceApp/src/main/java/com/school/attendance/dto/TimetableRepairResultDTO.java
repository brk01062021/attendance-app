package com.school.attendance.dto;

import java.util.ArrayList;
import java.util.List;

public class TimetableRepairResultDTO {
    private String batchId;
    private Integer conflictsBefore;
    private Integer conflictsAfter;
    private Integer repairedItems;
    private Boolean publishReady;
    private List<String> actions = new ArrayList<>();
    private TimetableGenerationResponseDTO timetable;

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public Integer getConflictsBefore() { return conflictsBefore; }
    public void setConflictsBefore(Integer conflictsBefore) { this.conflictsBefore = conflictsBefore; }
    public Integer getConflictsAfter() { return conflictsAfter; }
    public void setConflictsAfter(Integer conflictsAfter) { this.conflictsAfter = conflictsAfter; }
    public Integer getRepairedItems() { return repairedItems; }
    public void setRepairedItems(Integer repairedItems) { this.repairedItems = repairedItems; }
    public Boolean getPublishReady() { return publishReady; }
    public void setPublishReady(Boolean publishReady) { this.publishReady = publishReady; }
    public List<String> getActions() { return actions; }
    public void setActions(List<String> actions) { this.actions = actions == null ? new ArrayList<>() : actions; }
    public TimetableGenerationResponseDTO getTimetable() { return timetable; }
    public void setTimetable(TimetableGenerationResponseDTO timetable) { this.timetable = timetable; }
}
