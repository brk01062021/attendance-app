package com.school.attendance.dto;

public class TimetableEntryDTO {
    private String id;
    private String className;
    private String section;
    private String subjectName;
    private Long teacherId;
    private String teacherName;
    private String dayOfWeek;
    private Integer periodNumber;
    private String roomNumber;
    private String startTime;
    private String endTime;
    private Boolean isLab;
    private Boolean isSports;
    private Boolean conflict;

    public TimetableEntryDTO() {
    }

    public TimetableEntryDTO(String id, String className, String section, String subjectName, Long teacherId, String teacherName,
                             String dayOfWeek, Integer periodNumber, String roomNumber, String startTime, String endTime,
                             Boolean isLab, Boolean isSports, Boolean conflict) {
        this.id = id;
        this.className = className;
        this.section = section;
        this.subjectName = subjectName;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.dayOfWeek = dayOfWeek;
        this.periodNumber = periodNumber;
        this.roomNumber = roomNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isLab = isLab;
        this.isSports = isSports;
        this.conflict = conflict;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public Integer getPeriodNumber() { return periodNumber; }
    public void setPeriodNumber(Integer periodNumber) { this.periodNumber = periodNumber; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public Boolean getIsLab() { return isLab; }
    public void setIsLab(Boolean isLab) { this.isLab = isLab; }
    public Boolean getIsSports() { return isSports; }
    public void setIsSports(Boolean isSports) { this.isSports = isSports; }
    public Boolean getConflict() { return conflict; }
    public void setConflict(Boolean conflict) { this.conflict = conflict; }
}
