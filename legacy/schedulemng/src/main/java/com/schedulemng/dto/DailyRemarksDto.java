package com.schedulemng.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyRemarksDto {
    private String date;
    private String remarks;
} 