package com.showflix.api.log.scheduler;

import com.showflix.api.log.mapper.UserActionLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class UserActionLogCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(UserActionLogCleanupScheduler.class);
    private static final int RETENTION_DAYS = 14;

    private final UserActionLogMapper logMapper;

    public UserActionLogCleanupScheduler(UserActionLogMapper logMapper) {
        this.logMapper = logMapper;
    }

    /** 매일 새벽 3시에 2주 이전 로그 삭제 */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanup() {
        String cutoffDate = LocalDate.now().minusDays(RETENTION_DAYS)
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
        int deleted = logMapper.deleteOlderThan(cutoffDate);
        log.info("[ActionLog Cleanup] {}일 이전 로그 {}건 삭제 (기준: {})", RETENTION_DAYS, deleted, cutoffDate);
    }
}
