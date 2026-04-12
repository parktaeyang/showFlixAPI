package com.showflix.api.schedule.infrastructure;

import com.showflix.api.schedule.domain.VoucherTip;
import com.showflix.api.schedule.domain.VoucherTipRepository;
import com.showflix.api.schedule.mapper.VoucherTipMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Infrastructure Layer - VoucherTipRepository 구현체 (MyBatis)
 */
@Repository
public class VoucherTipMyBatisRepository implements VoucherTipRepository {

    private final VoucherTipMapper voucherTipMapper;

    public VoucherTipMyBatisRepository(VoucherTipMapper voucherTipMapper) {
        this.voucherTipMapper = voucherTipMapper;
    }

    @Override
    public List<VoucherTip> findByDate(String date) {
        return voucherTipMapper.findByDate(date);
    }

    @Override
    public List<VoucherTip> findByYearMonth(String yearMonth) {
        return voucherTipMapper.findByYearMonth(yearMonth);
    }

    @Override
    public List<VoucherTip> findByMonth(String startDate, String endDate) {
        return voucherTipMapper.findByMonth(startDate, endDate);
    }

    @Override
    public void upsert(VoucherTip voucherTip) {
        voucherTipMapper.upsert(voucherTip);
    }

    @Override
    public void upsertVoucher(VoucherTip voucherTip) {
        voucherTipMapper.upsertVoucher(voucherTip);
    }

    @Override
    public void upsertTip(VoucherTip voucherTip) {
        voucherTipMapper.upsertTip(voucherTip);
    }
}
