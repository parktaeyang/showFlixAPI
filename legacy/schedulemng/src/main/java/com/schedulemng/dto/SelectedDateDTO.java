package com.schedulemng.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SelectedDateDTO(
    String date,
    String userId,
    String userName,
    boolean openHope,
    String role,
    String accountType,
    String remarks
) {}