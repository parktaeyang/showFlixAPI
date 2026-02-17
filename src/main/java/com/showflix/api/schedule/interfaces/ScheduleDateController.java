package com.showflix.api.schedule.interfaces;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.showflix.api.auth.infrastructure.security.CustomUserDetails;
import com.showflix.api.schedule.application.SelectedDateService;
import com.showflix.api.schedule.application.command.MonthQueryCommand;
import com.showflix.api.schedule.application.command.SaveSelectedDatesCommand;
import com.showflix.api.schedule.interfaces.assembler.ScheduleDateAssembler;
import com.showflix.api.schedule.interfaces.dto.MonthDataResponse;
import com.showflix.api.schedule.interfaces.dto.SelectedDateResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Interfaces Layer - 선택 날짜 API 엔드포인트
 */
@RestController
@RequestMapping("/api/schedule/dates")
public class ScheduleDateController {

    private final SelectedDateService selectedDateService;

    public ScheduleDateController(SelectedDateService selectedDateService) {
        this.selectedDateService = selectedDateService;
    }

    /**
     * 월별 데이터 조회
     * GET /api/schedule/dates/month?year=2025&month=2
     */
    @GetMapping("/month")
    public ResponseEntity<MonthDataResponse> getMonthData(
            @RequestParam int year,
            @RequestParam int month) {
        MonthQueryCommand command = new MonthQueryCommand(year, month);
        SelectedDateService.MonthResult result = selectedDateService.getDatesByMonth(command);
        List<SelectedDateResponse> data = ScheduleDateAssembler.toResponseList(result.getData());
        return ResponseEntity.ok(new MonthDataResponse(result.isAdmin(), data));
    }

    /**
     * 선택 날짜 저장
     * POST /api/schedule/dates/save
     * 요청 본문: { "2025-02-15": { "openHope": true }, "2025-02-16": { "openHope": false } }
     */
    @PostMapping("/save")
    public ResponseEntity<Void> saveSelectedDates(@RequestBody Map<String, DateSelectionPayload> payload) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails cud)) {
            return ResponseEntity.status(401).build();
        }
        var user = cud.getUser();
        Map<String, SaveSelectedDatesCommand.DateSelection> dateSelections = payload.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new SaveSelectedDatesCommand.DateSelection(
                                e.getValue() != null && e.getValue().openHope)
                ));
        SaveSelectedDatesCommand command = new SaveSelectedDatesCommand(
                user.getUserid(),
                user.getUsername(),
                "", // role - 추후 User에 role 추가 시 연동
                dateSelections
        );
        selectedDateService.saveSelectedDates(command);
        return ResponseEntity.ok().build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DateSelectionPayload(boolean openHope) {}
}
