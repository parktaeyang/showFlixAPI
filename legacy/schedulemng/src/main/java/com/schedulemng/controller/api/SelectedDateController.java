package com.schedulemng.controller.api;

import com.schedulemng.dto.MonthDataResponseDTO;
import com.schedulemng.dto.SelectedDateDTO;

import com.schedulemng.entity.AdminNote;
import com.schedulemng.entity.SelectedDate;
import com.schedulemng.entity.User;
import com.schedulemng.entity.ScheduleTimeSlot;

import com.schedulemng.security.CustomUserDetails;
import com.schedulemng.service.ScheduleTimeSlotService;
import com.schedulemng.service.SelectedDateService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/dates")
@RequiredArgsConstructor
public class SelectedDateController {

    private final SelectedDateService service;

    private final ScheduleTimeSlotService scheduleTimeSlotService;

    @PersistenceContext
    private EntityManager em;

    @PostMapping("/save")
    public ResponseEntity<?> saveSelectedDates(@RequestBody Map<String, SelectedDateDTO> selectedDates) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        User user = userDetails.getUser();

        List<SelectedDate> selectedDateList = new ArrayList<>();
        selectedDates.forEach((date, dto) -> {
            log.info(
                    "날짜: " + date
                + ", 사용자: " + user.getUsername()
                + ", 역할: " + user.getRole()
                + ", 오픈희망: " + dto.openHope());

            SelectedDate selectedDate = new SelectedDate();
            selectedDate.setDate(date);
            selectedDate.setUserId(user.getUserid());
            selectedDate.setUserName(user.getUsername());
            selectedDate.setRole(user.getRole().toString());
            selectedDate.setOpenHope(dto.openHope());

            selectedDateList.add(selectedDate);
        });
        service.saveAll(selectedDateList);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/month")
    public ResponseEntity<MonthDataResponseDTO> getDatesByMonth(
            @RequestParam int year,
            @RequestParam int month,
            HttpSession session) {

        List<SelectedDate> monthlyData = service.getDatesByMonth(year, month);

        // 관리자 판별: AccountType 이 CAPTAIN 또는 ADMIN
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean adminFlag = false;
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
            User u = cud.getUser();
            adminFlag = (u.getAccountType() == User.AccountType.CAPTAIN) || (u.getAccountType() == User.AccountType.ADMIN);
        }

        // 일반 사용자라면 비확정 데이터는 본인 것만 보이게 필터링
        if (!adminFlag) {
            String currentUserId;
            if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
                User u = cud.getUser();
                currentUserId = u.getUserid();
            } else {
                currentUserId = "";
            }

            if (currentUserId != null) {
                monthlyData = monthlyData.stream()
                        .filter(d -> {
                            // 확정된 날짜는 모두 노출, 미확정은 본인 것만
                            String confirmed = d.getConfirmed();
                            boolean isConfirmed = confirmed != null && confirmed.equalsIgnoreCase("Y");
                            return isConfirmed || currentUserId.equals(d.getUserId());
                        })
                        .toList();
            }
        }

        MonthDataResponseDTO response = new MonthDataResponseDTO(adminFlag, monthlyData);

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 날짜의 시간표 데이터를 조회하는 API 엔드포인트.
     * GET /api/schedule/time-slots?date=YYYY-MM-DD
     *
     * @param date 조회할 날짜 (YYYY-MM-DD 형식의 문자열)
     * @return 해당 날짜의 시간표 목록
     */
    @GetMapping("/time-slots")
    public ResponseEntity<List<ScheduleTimeSlot>> getTimeSlots(@RequestParam String date) {
        // 로그인 사용자 정보
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName;
        boolean isAdminUser = false;
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
            User u = cud.getUser();
            currentUserName = u.getUsername();
            // 역할 판별: User에 roles 컬렉션이 없을 수 있으므로 role 필드 우선 사용
            try {
                isAdminUser = (u.getAccountType() == User.AccountType.CAPTAIN) || (u.getAccountType() == User.AccountType.ADMIN);
            } catch (Exception ignore) { }
        } else {
            currentUserName = "";
        }

        boolean confirmed = scheduleTimeSlotService.isDateConfirmed(date);
        List<ScheduleTimeSlot> timeSlots = scheduleTimeSlotService.getTimeSlotsByDate(date);

        if (!confirmed && !isAdminUser && currentUserName != null && !currentUserName.isEmpty()) {
            List<ScheduleTimeSlot> filtered = timeSlots.stream()
                    .filter(ts -> ts.getPerformer() != null && containsPerformer(ts.getPerformer(), currentUserName))
                    .map(ts -> {
                        ScheduleTimeSlot s = new ScheduleTimeSlot();
                        s.setId(ts.getId());
                        s.setTheme(ts.getTheme());
                        // 본인 이름만 노출
                        s.setPerformer(currentUserName);
                        s.setConfirmed(ts.getConfirmed());
                        return s;
                    })
                    .toList();
            return ResponseEntity.ok(filtered);
        }

        return ResponseEntity.ok(timeSlots);
    }

    private boolean containsPerformer(String performerCsv, String name) {
        if (performerCsv == null || name == null) return false;
        for (String p : performerCsv.split(",")) {
            if (name.equals(p.trim())) return true;
        }
        return false;
    }

    /**
     * 시간표 데이터를 저장하거나 업데이트하는 API 엔드포인트.
     * POST /api/schedule/time-slots/save
     * 요청 본문 예시:
     * {
     * "2025-08-03": [
     * { "time": "16:00", "theme": "테마1", "performer": "출연자1" },
     * { "time": "16:30", "theme": "테마2", "performer": "출연자2" }
     * ]
     * }
     *
     * @param payload 날짜별 시간표 데이터
     * @return 저장 성공 메시지
     */
    @PostMapping("/time-slots/save")
    public ResponseEntity<String> saveTimeSlots(@RequestBody Map<String, List<Map<String, String>>> payload) {
        // 클라이언트에서 보낸 payload의 첫 번째 키가 날짜 문자열이라고 가정합니다.
        String dateStr = payload.keySet().iterator().next();
        List<Map<String, String>> timeSlotDataList = payload.get(dateStr);

        scheduleTimeSlotService.saveOrUpdateScheduleTimeSlots(dateStr, timeSlotDataList);
        return ResponseEntity.ok("시간표 저장 완료");
    }

    /**
     * 특정 날짜의 시간표 확정 상태를 변경하는 API 엔드포인트.
     * POST /api/dates/time-slots/confirm
     * 요청 본문 예시: { "date": "2025-08-03", "confirmed": "Y" } 또는 { "date": "2025-08-03", "confirmed": "N" }
     *
     * @param payload 날짜와 확정 여부를 담은 Map
     * @return 처리 완료 메시지
     */
    @PostMapping("/time-slots/confirm")
    public ResponseEntity<String> updateTimeSlotsConfirmation(@RequestBody Map<String, String> payload) {
        String dateStr = payload.get("date");
        String confirmed = payload.get("confirmed");
        
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("날짜가 필요합니다.");
        }
        
        if (confirmed == null || (!confirmed.equals("Y") && !confirmed.equals("N"))) {
            return ResponseEntity.badRequest().body("확정 여부는 Y 또는 N이어야 합니다.");
        }

        scheduleTimeSlotService.updateTimeSlotsConfirmationByDate(dateStr, confirmed);
        // 선택일자도 동일하게 처리
        service.updateConfirmationByDate(dateStr, confirmed);
        
        String message = "Y".equals(confirmed) ? "시간표 확정 완료" : "시간표 확정 취소 완료";
        return ResponseEntity.ok(message);
    }

    @PostMapping("/roles/save")
    public ResponseEntity<?> saveRoles(@RequestBody List<SelectedDateDTO> updates) {

        List<SelectedDate> entities = new ArrayList<>(updates.size());

        String remarksForDate = null;
        String dateStr = null;
        for (SelectedDateDTO dto : updates) {
            SelectedDate entity = new SelectedDate();
            entity.setDate(dto.date());
            entity.setUserId(dto.userId());
            entity.setRole(dto.role());
            entity.setOpenHope(dto.openHope());
            entity.setRemarks(dto.remarks());
            entities.add(entity);

            log.debug("roles/save payload => date={}, userId={}, userName={}, role={}", dto.date(), dto.userId(), dto.userName(), dto.role());
            int updated = service.updateRoleByDateAndUserId(
                    dto.date(), dto.userId(), dto.role(), dto.remarks()
            );

            if (updated == 0) {
                SelectedDate e = new SelectedDate();
                e.setDate(dto.date());
                e.setUserId(dto.userId());
                e.setUserName(dto.userName());
                e.setOpenHope(dto.openHope());
                e.setRole(dto.role());
                e.setRemarks(dto.remarks());
                service.save(e);
            }

            // 날짜 비고 동시 저장 (첫 항목의 날짜 기준)
            if (dateStr == null) dateStr = dto.date();
            if (dto.remarks() != null) {
                remarksForDate = dto.remarks();
            }
        }

        // 관리자만 비고 저장 허용
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = false;
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
            User u = cud.getUser();
            isAdmin = (u.getAccountType() == User.AccountType.CAPTAIN) || (u.getAccountType() == User.AccountType.ADMIN);
        }
        if (isAdmin && dateStr != null && remarksForDate != null) {
            service.saveOrUpdateDailyRemarks(dateStr, remarksForDate);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/saveAdminNote")
    public ResponseEntity<?> saveAdminNote(@RequestBody Map<String, String> body) {
        String content = body.getOrDefault("content", "");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        User user = userDetails.getUser();

        AdminNote adminNote = new AdminNote();
        adminNote.setContent(content);
        adminNote.setUpdatedBy(user.getUserid());

        service.saveAdminNote(adminNote);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/getAdminNote")
    public ResponseEntity<AdminNote> getAdminNote() {
        AdminNote response = service.getAdminNote();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/selection")
    public ResponseEntity<?> deleteSelection(@RequestParam String date, @RequestParam String userId) {
        int deleted = service.deleteByDateAndUserId(date, userId);
        if (deleted == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }
    /**
     * 현재 로그인한 사용자 정보 조회
     * GET /api/dates/current-user
     */
    @GetMapping("/current-user")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> userInfo = new HashMap<>();
        
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
            User u = cud.getUser();
            userInfo.put("userId", u.getUserid());
            userInfo.put("userName", u.getUsername());
            userInfo.put("accountType", u.getAccountType());
            userInfo.put("isAdmin", (u.getAccountType() == User.AccountType.CAPTAIN) || (u.getAccountType() == User.AccountType.ADMIN));
        }
        
        return ResponseEntity.ok(userInfo);
    }

    /**
     * 전체 사용자 목록 (id, name, accountType) 조회
     * GET /api/dates/users
     */
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> listUsers() {
        List<User> userList = em.createQuery("select u from User u", User.class).getResultList();

        List<Map<String, Object>> out = userList.stream()
                .map(user -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("userId", user.getUserid());
                    m.put("userName", user.getUsername());
                    m.put("accountType", user.getAccountType());
                    return m;
                })
                .toList();

        return ResponseEntity.ok(out);
    }
}