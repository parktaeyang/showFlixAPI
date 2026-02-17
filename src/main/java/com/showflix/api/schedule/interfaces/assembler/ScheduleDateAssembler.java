package com.showflix.api.schedule.interfaces.assembler;

import com.showflix.api.schedule.domain.SelectedDate;
import com.showflix.api.schedule.interfaces.dto.SelectedDateResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Interfaces Layer - Schedule Date DTO 변환 Assembler
 */
public final class ScheduleDateAssembler {

    private ScheduleDateAssembler() {}

    public static SelectedDateResponse toResponse(SelectedDate domain) {
        if (domain == null) return null;
        return new SelectedDateResponse(
                domain.getDate(),
                domain.getUserId(),
                domain.getUserName(),
                domain.isOpenHope(),
                domain.getRole(),
                domain.getConfirmed(),
                domain.getRemarks()
        );
    }

    public static List<SelectedDateResponse> toResponseList(List<SelectedDate> domains) {
        if (domains == null) return List.of();
        return domains.stream().map(ScheduleDateAssembler::toResponse).collect(Collectors.toList());
    }
}
