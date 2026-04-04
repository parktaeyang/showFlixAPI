package com.showflix.api.admin.interfaces;

import com.showflix.api.admin.application.MembershipService;
import com.showflix.api.admin.domain.Membership;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Interfaces Layer - 멤버십 회원 API
 * 경로: /api/admin/membership
 */
@RestController
@RequestMapping("/api/admin/membership")
@PreAuthorize("hasRole('ADMIN')")
public class MembershipController {

    private final MembershipService service;

    public MembershipController(MembershipService service) {
        this.service = service;
    }

    record MembershipResponse(Long id, String memberName, String phone,
                              String joinDate, String expireDate, String memo) {}

    record CreateMembershipRequest(String memberName, String phone,
                                   String joinDate, String expireDate, String memo) {}

    record UpdateMembershipRequest(String memberName, String phone,
                                   String joinDate, String expireDate, String memo) {}

    /** GET /api/admin/membership */
    @GetMapping
    public ResponseEntity<List<MembershipResponse>> getAll() {
        List<MembershipResponse> responses = service.getAll().stream()
                .map(e -> new MembershipResponse(
                        e.getId(), e.getMemberName(), e.getPhone(),
                        e.getJoinDate(), e.getExpireDate(), e.getMemo()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /** POST /api/admin/membership */
    @PostMapping
    public ResponseEntity<Map<String, String>> create(@RequestBody CreateMembershipRequest request) {
        service.create(request.memberName(), request.phone(),
                request.joinDate(), request.expireDate(), request.memo());
        return ResponseEntity.ok(Map.of("message", "멤버십 회원이 등록되었습니다."));
    }

    /** PUT /api/admin/membership/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> update(
            @PathVariable Long id, @RequestBody UpdateMembershipRequest request) {
        service.update(id, request.memberName(), request.phone(),
                request.joinDate(), request.expireDate(), request.memo());
        return ResponseEntity.ok(Map.of("message", "멤버십 회원이 수정되었습니다."));
    }

    /** DELETE /api/admin/membership/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "멤버십 회원이 삭제되었습니다."));
    }
}
