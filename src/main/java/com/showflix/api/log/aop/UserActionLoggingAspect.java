package com.showflix.api.log.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.showflix.api.log.application.UserActionLogService;
import com.showflix.api.log.domain.UserActionLog;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Aspect
@Component
public class UserActionLoggingAspect {

    private static final int MAX_REQUEST_DATA_LENGTH = 2000;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    private final UserActionLogService userActionLogService;

    public UserActionLoggingAspect(UserActionLogService userActionLogService) {
        this.userActionLogService = userActionLogService;
    }

    /** 포인트컷: application 패키지 서비스의 CUD 메서드 */
    @Pointcut(
        "execution(* com.showflix.api..application.*Service.create*(..)) || " +
        "execution(* com.showflix.api..application.*Service.update*(..)) || " +
        "execution(* com.showflix.api..application.*Service.delete*(..)) || " +
        "execution(* com.showflix.api..application.*Service.save*(..))   || " +
        "execution(* com.showflix.api..application.*Service.confirm*(..))"
    )
    public void cudMethods() {}

    /** 성공 로깅 — 정상 완료된 트랜잭션만 기록 */
    @AfterReturning("cudMethods()")
    public void logAction(JoinPoint joinPoint) {
        try {
            userActionLogService.log(buildLog(joinPoint, resolveAction(joinPoint.getSignature().getName()), null));
        } catch (Exception ignored) { /* 로깅 실패는 비즈니스 로직에 영향 없음 */ }
    }

    /**
     * 실패 로깅 — 예외 발생 시 action = "FAILED"로 기록
     * 비즈니스 트랜잭션은 이후 롤백되지만 FAILED 로그는 독립 커밋으로 보존
     */
    @AfterThrowing(pointcut = "cudMethods()", throwing = "ex")
    public void logFailedAction(JoinPoint joinPoint, Exception ex) {
        try {
            userActionLogService.log(buildLog(joinPoint, "FAILED", ex.getMessage()));
        } catch (Exception ignored) { /* 로깅 실패는 무시 */ }
    }

    // ── helpers ──────────────────────────────────────────────────────

    private UserActionLog buildLog(JoinPoint joinPoint, String action, String errorMsg) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username    = (auth != null && auth.isAuthenticated()) ? auth.getName() : "anonymous";
        String serviceName = joinPoint.getTarget().getClass().getSimpleName();
        String methodName  = joinPoint.getSignature().getName();

        UserActionLog log = new UserActionLog();
        log.setUsername(username);
        log.setAction(action);
        log.setTargetTable(TABLE_MAP.getOrDefault(serviceName, "unknown"));
        log.setDescription(errorMsg != null
            ? serviceName + "." + methodName + " FAILED: " + errorMsg
            : serviceName + "." + methodName);
        log.setRequestData(serializeArgs(joinPoint));
        return log;
    }

    private String serializeArgs(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args == null || args.length == 0) return null;

            String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
            Map<String, Object> params = new LinkedHashMap<>();
            for (int i = 0; i < args.length; i++) {
                String name = (paramNames != null && i < paramNames.length) ? paramNames[i] : "arg" + i;
                params.put(name, args[i]);
            }

            String json = OBJECT_MAPPER.writeValueAsString(params);
            if (json.length() > MAX_REQUEST_DATA_LENGTH) {
                return json.substring(0, MAX_REQUEST_DATA_LENGTH) + "...(truncated)";
            }
            return json;
        } catch (Exception e) {
            return "[serialization failed: " + e.getMessage() + "]";
        }
    }

    private String resolveAction(String methodName) {
        if (methodName.startsWith("create") || methodName.startsWith("insert")
                || methodName.startsWith("save")) return "CREATE";
        if (methodName.startsWith("update") || methodName.startsWith("confirm")) return "UPDATE";
        if (methodName.startsWith("delete") || methodName.startsWith("remove")) return "DELETE";
        return "UNKNOWN";
    }

    private static final Map<String, String> TABLE_MAP = Map.ofEntries(
        Map.entry("ScheduleService",         "sf_schedule"),
        Map.entry("ScheduleTimeSlotService", "sf_time_slot"),
        Map.entry("SelectedDateService",     "sf_selected_date"),
        Map.entry("VoucherTipService",       "sf_voucher_tip"),
        Map.entry("WorkDiaryService",        "sf_work_diary"),
        Map.entry("MembershipService",       "sf_membership"),
        Map.entry("PartnerService",          "sf_partner"),
        Map.entry("BeerSelectService",       "sf_beer_select"),
        Map.entry("HealthCertService",       "sf_health_cert"),
        Map.entry("DailyNoteService",        "sf_daily_note"),
        Map.entry("MonthlyNoteService",      "sf_monthly_note"),
        Map.entry("AngelShowCancelService",  "sf_angel_show_cancel"),
        Map.entry("XmasSeatService",         "sf_xmas_seat")
    );
}
