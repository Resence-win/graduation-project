package com.qms.campuscard.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class StudentImportResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer totalCount = 0;
    private Integer successCount = 0;
    private Integer skippedCount = 0;
    private Integer failedCount = 0;
    private List<String> failureReasons = new ArrayList<>();

    public void increaseTotal() {
        totalCount++;
    }

    public void increaseSuccess() {
        successCount++;
    }

    public void increaseSkipped() {
        skippedCount++;
    }

    public void addFailure(String reason) {
        failedCount++;
        failureReasons.add(reason);
    }
}
