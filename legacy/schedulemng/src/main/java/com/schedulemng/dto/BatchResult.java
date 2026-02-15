package com.schedulemng.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchResult {
    private boolean success;
    private int totalCount;
    private int successCount;
    private int failureCount;
    private String message;
    
    public BatchResult(boolean success, int totalCount, int successCount, int failureCount) {
        this.success = success;
        this.totalCount = totalCount;
        this.successCount = successCount;
        this.failureCount = failureCount;
    }
} 