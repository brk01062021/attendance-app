package com.school.attendance.dto;

import java.util.ArrayList;
import java.util.List;

public class ClassTeacherPoolDTO {
    private String poolId;
    private String poolName;
    private String className;
    private List<Long> teacherIds = new ArrayList<>();
    private List<String> teacherNames = new ArrayList<>();

    public ClassTeacherPoolDTO() {
    }

    public ClassTeacherPoolDTO(String poolId, String poolName, String className, List<Long> teacherIds, List<String> teacherNames) {
        this.poolId = poolId;
        this.poolName = poolName;
        this.className = className;
        this.teacherIds = teacherIds == null ? new ArrayList<>() : teacherIds;
        this.teacherNames = teacherNames == null ? new ArrayList<>() : teacherNames;
    }

    public String getPoolId() { return poolId; }
    public void setPoolId(String poolId) { this.poolId = poolId; }
    public String getPoolName() { return poolName; }
    public void setPoolName(String poolName) { this.poolName = poolName; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public List<Long> getTeacherIds() { return teacherIds; }
    public void setTeacherIds(List<Long> teacherIds) { this.teacherIds = teacherIds == null ? new ArrayList<>() : teacherIds; }
    public List<String> getTeacherNames() { return teacherNames; }
    public void setTeacherNames(List<String> teacherNames) { this.teacherNames = teacherNames == null ? new ArrayList<>() : teacherNames; }
}
