package com.showflix.api.log.application;

import com.showflix.api.log.domain.UserActionLog;
import com.showflix.api.log.mapper.UserActionLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class UserActionLogService {

    private final UserActionLogMapper logMapper;

    public UserActionLogService(UserActionLogMapper logMapper) {
        this.logMapper = logMapper;
    }

    /** 로그 저장 — 새 트랜잭션으로 분리, 비즈니스 롤백과 무관하게 독립 커밋 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(UserActionLog log) {
        logMapper.insert(log);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> search(String username, String action,
                                      String startDate, String endDate,
                                      int page, int size) {
        int offset = page * size;
        return Map.of(
            "items", logMapper.findByFilter(username, action, startDate, endDate, offset, size),
            "total", logMapper.countByFilter(username, action, startDate, endDate),
            "page",  page,
            "size",  size
        );
    }
}
