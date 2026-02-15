package com.schedulemng.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDto {
    private String date;
    private String username;
    private String hours;
    private String remarks;
    
    public ScheduleDto(String date, String username, String hours) {
        this.date = date;
        this.username = username;
        this.hours = hours;
    }
} 