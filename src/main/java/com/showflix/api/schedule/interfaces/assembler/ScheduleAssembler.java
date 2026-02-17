package com.showflix.api.schedule.interfaces.assembler;

import com.showflix.api.schedule.application.ScheduleService;
import com.showflix.api.schedule.application.command.DeleteScheduleCommand;
import com.showflix.api.schedule.application.command.SaveScheduleCommand;
import com.showflix.api.schedule.application.command.ScheduleTableQueryCommand;
import com.showflix.api.schedule.application.command.UpdateDailyRemarksCommand;
import com.showflix.api.schedule.interfaces.dto.SaveScheduleRequest;
import com.showflix.api.schedule.interfaces.dto.ScheduleRowResponse;
import com.showflix.api.schedule.interfaces.dto.ScheduleTableResponse;
import com.showflix.api.schedule.interfaces.dto.UpdateDailyRemarksRequest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Interfaces Layer - Schedule DTO ↔ Command/Result 변환기
 */
public final class ScheduleAssembler {

    private ScheduleAssembler() {}

    public static ScheduleTableQueryCommand toTableQueryCommand(int year, int month) {
        return new ScheduleTableQueryCommand(year, month);
    }

    public static SaveScheduleCommand toSaveCommand(SaveScheduleRequest request) {
        return new SaveScheduleCommand(
                request.getDate(),
                request.getUsername(),
                request.getHours(),
                request.getRemarks()
        );
    }

    public static DeleteScheduleCommand toDeleteCommand(String date, String username) {
        return new DeleteScheduleCommand(date, username);
    }

    public static UpdateDailyRemarksCommand toUpdateRemarksCommand(
            UpdateDailyRemarksRequest request) {
        return new UpdateDailyRemarksCommand(request.getDate(), request.getRemarks());
    }

    public static ScheduleTableResponse toTableResponse(
            ScheduleService.ScheduleTableResult result) {
        List<ScheduleRowResponse> rows = result.getRows().stream()
                .map(row -> new ScheduleRowResponse(
                        row.getDate(),
                        row.getDayOfWeek(),
                        row.getActorHours(),
                        row.getRowTotal(),
                        row.getRemarks()
                ))
                .collect(Collectors.toList());

        return new ScheduleTableResponse(
                result.getYear(),
                result.getMonth(),
                result.getActorNames(),
                rows,
                result.getColumnTotals(),
                result.getGrandTotal()
        );
    }
}
