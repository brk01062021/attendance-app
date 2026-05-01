package com.school.attendance.dto;

import java.util.List;

public class GroupedReplacementOptionsDTO {

    private List<ReplacementTeacherDTO> bestMatch;
    private List<ReplacementTeacherDTO> sameClass;
    private List<ReplacementTeacherDTO> others;

    public GroupedReplacementOptionsDTO(
            List<ReplacementTeacherDTO> bestMatch,
            List<ReplacementTeacherDTO> sameClass,
            List<ReplacementTeacherDTO> others
    ) {
        this.bestMatch = bestMatch;
        this.sameClass = sameClass;
        this.others = others;
    }

    public List<ReplacementTeacherDTO> getBestMatch() {
        return bestMatch;
    }

    public List<ReplacementTeacherDTO> getSameClass() {
        return sameClass;
    }

    public List<ReplacementTeacherDTO> getOthers() {
        return others;
    }
}