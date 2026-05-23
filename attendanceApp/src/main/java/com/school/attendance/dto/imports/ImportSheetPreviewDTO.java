package com.school.attendance.dto.imports;

import java.util.ArrayList;
import java.util.List;

public class ImportSheetPreviewDTO {
    private String sheetName;
    private int totalRows;
    private List<String> headers = new ArrayList<>();

    public ImportSheetPreviewDTO() { }

    public ImportSheetPreviewDTO(String sheetName, int totalRows, List<String> headers) {
        this.sheetName = sheetName;
        this.totalRows = totalRows;
        this.headers = headers == null ? new ArrayList<>() : headers;
    }

    public String getSheetName() { return sheetName; }
    public void setSheetName(String sheetName) { this.sheetName = sheetName; }
    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }
    public List<String> getHeaders() { return headers; }
    public void setHeaders(List<String> headers) { this.headers = headers; }
}
