package com.schedulemng.entity;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SelectedDateId implements Serializable {
    private String date;
    private String userId;
}