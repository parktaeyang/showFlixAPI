package com.showflix.api.admin.interfaces;

import com.showflix.api.admin.application.HealthCertService;
import com.showflix.api.admin.domain.HealthCert;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Interfaces Layer - 보건증 관리 API
 * 경로: /api/admin/health-cert
 */
@RestController
@RequestMapping("/api/admin/health-cert")
@PreAuthorize("hasRole('ADMIN')")
public class HealthCertController {

    private final HealthCertService service;

    public HealthCertController(HealthCertService service) {
        this.service = service;
    }

    record HealthCertResponse(String userId, String userName,
                              String expireDate, String notes) {}

    record UpdateHealthCertRequest(String userId, String expireDate, String notes) {}

    /** GET /api/admin/health-cert */
    @GetMapping
    public ResponseEntity<List<HealthCertResponse>> getAll() {
        List<HealthCertResponse> responses = service.getAll().stream()
                .map(e -> new HealthCertResponse(
                        e.getUserId(), e.getUserName(),
                        e.getExpireDate(), e.getNotes()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /** PUT /api/admin/health-cert */
    @PutMapping
    public ResponseEntity<Map<String, String>> update(@RequestBody UpdateHealthCertRequest request) {
        service.update(request.userId(), request.expireDate(), request.notes());
        return ResponseEntity.ok(Map.of("message", "보건증 정보가 수정되었습니다."));
    }
}
