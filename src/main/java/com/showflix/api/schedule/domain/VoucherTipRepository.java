package com.showflix.api.schedule.domain;

import java.util.List;

/**
 * Domain Layer - VoucherTipRepository Port
 */
public interface VoucherTipRepository {

    List<VoucherTip> findByDate(String date);

    List<VoucherTip> findByYearMonth(String yearMonth);

    List<VoucherTip> findByMonth(String startDate, String endDate);

    void upsert(VoucherTip voucherTip);

    void upsertVoucher(VoucherTip voucherTip);

    void upsertTip(VoucherTip voucherTip);
}
