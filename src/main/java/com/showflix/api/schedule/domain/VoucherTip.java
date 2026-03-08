package com.showflix.api.schedule.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * Domain Layer - 배우 바우처/팁 도메인 모델
 * 테이블: actor_voucher_tip
 */
@Getter
@Setter
public class VoucherTip {

    private Long id;
    private String date;
    private String userId;
    private String userName;
    private int voucher;
    private int tip;
}
