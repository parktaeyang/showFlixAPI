package com.showflix.api.admin.interfaces;

import com.showflix.api.admin.application.PartnerService;
import com.showflix.api.admin.domain.Partner;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Interfaces Layer - 협력업체 API
 * 경로: /api/admin/partners
 */
@RestController
@RequestMapping("/api/admin/partners")
@PreAuthorize("hasRole('ADMIN')")
public class PartnerController {

    private final PartnerService service;

    public PartnerController(PartnerService service) {
        this.service = service;
    }

    record PartnerResponse(Long id, String category, String name,
                           String contact, String manager, String notes) {}

    record CreatePartnerRequest(String category, String name,
                                String contact, String manager, String notes) {}

    record UpdatePartnerRequest(String category, String name,
                                String contact, String manager, String notes) {}

    /** GET /api/admin/partners */
    @GetMapping
    public ResponseEntity<List<PartnerResponse>> getAll() {
        List<PartnerResponse> responses = service.getAll().stream()
                .map(e -> new PartnerResponse(
                        e.getId(), e.getCategory(), e.getName(),
                        e.getContact(), e.getManager(), e.getNotes()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /** POST /api/admin/partners */
    @PostMapping
    public ResponseEntity<Map<String, String>> create(@RequestBody CreatePartnerRequest request) {
        service.create(request.category(), request.name(),
                request.contact(), request.manager(), request.notes());
        return ResponseEntity.ok(Map.of("message", "협력업체가 등록되었습니다."));
    }

    /** PUT /api/admin/partners/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> update(
            @PathVariable Long id, @RequestBody UpdatePartnerRequest request) {
        service.update(id, request.category(), request.name(),
                request.contact(), request.manager(), request.notes());
        return ResponseEntity.ok(Map.of("message", "협력업체가 수정되었습니다."));
    }

    /** DELETE /api/admin/partners/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "협력업체가 삭제되었습니다."));
    }
}
