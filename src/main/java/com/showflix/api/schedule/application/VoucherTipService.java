package com.showflix.api.schedule.application;

import com.showflix.api.schedule.domain.SelectedDate;
import com.showflix.api.schedule.domain.SelectedDateRepository;
import com.showflix.api.schedule.domain.VoucherTip;
import com.showflix.api.schedule.domain.VoucherTipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Application Layer - 바우처/팁 유스케이스 서비스
 */
@Service
public class VoucherTipService {

    private final SelectedDateRepository selectedDateRepository;
    private final VoucherTipRepository voucherTipRepository;

    public VoucherTipService(SelectedDateRepository selectedDateRepository,
                             VoucherTipRepository voucherTipRepository) {
        this.selectedDateRepository = selectedDateRepository;
        this.voucherTipRepository = voucherTipRepository;
    }

    /**
     * 특정 날짜 출근자 목록 + 기존 바우처/팁 값 병합 조회
     */
    @Transactional(readOnly = true)
    public List<VoucherTipEntry> getByDate(String date) {
        // 해당 날짜 출근자 목록 (selected_date)
        List<SelectedDate> actors = selectedDateRepository.findByDateBetween(date, date);

        // 기존 저장된 바우처/팁 데이터 (user_id → VoucherTip)
        Map<String, VoucherTip> savedMap = voucherTipRepository.findByDate(date)
                .stream()
                .collect(Collectors.toMap(VoucherTip::getUserId, vt -> vt));

        return actors.stream()
                .map(actor -> {
                    VoucherTip saved = savedMap.get(actor.getUserId());
                    VoucherTipEntry entry = new VoucherTipEntry();
                    entry.setUserId(actor.getUserId());
                    entry.setUserName(actor.getUserName());
                    entry.setRole(actor.getRole());
                    entry.setVoucher(saved != null ? saved.getVoucher() : 0);
                    entry.setTip(saved != null ? saved.getTip() : 0);
                    return entry;
                })
                .collect(Collectors.toList());
    }

    /**
     * 바우처/팁 일괄 저장 (upsert)
     */
    @Transactional
    public void saveAll(String date, List<SaveEntry> entries) {
        if (date == null || date.isBlank()) {
            throw new IllegalArgumentException("날짜를 입력해주세요.");
        }
        for (SaveEntry entry : entries) {
            VoucherTip vt = new VoucherTip();
            vt.setDate(date);
            vt.setUserId(entry.userId());
            vt.setUserName(entry.userName());
            vt.setVoucher(entry.voucher());
            vt.setTip(entry.tip());
            voucherTipRepository.upsert(vt);
        }
    }

    // ─── Result / Command DTO ───────────────────────────────────────

    /**
     * 조회 결과 DTO
     */
    public static class VoucherTipEntry {
        private String userId;
        private String userName;
        private String role;
        private int voucher;
        private int tip;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public int getVoucher() { return voucher; }
        public void setVoucher(int voucher) { this.voucher = voucher; }

        public int getTip() { return tip; }
        public void setTip(int tip) { this.tip = tip; }
    }

    /**
     * 저장 요청 Command
     */
    public record SaveEntry(String userId, String userName, int voucher, int tip) {}
}
