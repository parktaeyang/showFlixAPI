package com.showflix.api.admin.interfaces;

import com.showflix.api.admin.application.BeerSelectService;
import com.showflix.api.admin.domain.BeerSelect;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Interfaces Layer - 맥주 셀렉 API
 * 경로: /api/admin/beer-select
 */
@RestController
@RequestMapping("/api/admin/beer-select")
@PreAuthorize("hasRole('ADMIN')")
public class BeerSelectController {

    private final BeerSelectService service;

    public BeerSelectController(BeerSelectService service) {
        this.service = service;
    }

    record BeerSelectResponse(Long id, String beerName, String brand,
                              String category, String notes, boolean active) {}

    record CreateBeerSelectRequest(String beerName, String brand,
                                   String category, String notes, Boolean active) {}

    record UpdateBeerSelectRequest(String beerName, String brand,
                                   String category, String notes, Boolean active) {}

    /** GET /api/admin/beer-select */
    @GetMapping
    public ResponseEntity<List<BeerSelectResponse>> getAll() {
        List<BeerSelectResponse> responses = service.getAll().stream()
                .map(e -> new BeerSelectResponse(
                        e.getId(), e.getBeerName(), e.getBrand(),
                        e.getCategory(), e.getNotes(), e.isActive()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /** POST /api/admin/beer-select */
    @PostMapping
    public ResponseEntity<Map<String, String>> create(@RequestBody CreateBeerSelectRequest request) {
        service.create(request.beerName(), request.brand(),
                request.category(), request.notes(),
                request.active() != null ? request.active() : true);
        return ResponseEntity.ok(Map.of("message", "맥주가 등록되었습니다."));
    }

    /** PUT /api/admin/beer-select/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> update(
            @PathVariable Long id, @RequestBody UpdateBeerSelectRequest request) {
        service.update(id, request.beerName(), request.brand(),
                request.category(), request.notes(),
                request.active() != null ? request.active() : true);
        return ResponseEntity.ok(Map.of("message", "맥주가 수정되었습니다."));
    }

    /** DELETE /api/admin/beer-select/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "맥주가 삭제되었습니다."));
    }
}
