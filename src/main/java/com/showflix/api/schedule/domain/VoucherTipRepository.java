package com.showflix.api.schedule.domain;

import java.util.List;

/**
 * Domain Layer - VoucherTipRepository Port
 */
public interface VoucherTipRepository {

    List<VoucherTip> findByDate(String date);

    void upsert(VoucherTip voucherTip);
}
