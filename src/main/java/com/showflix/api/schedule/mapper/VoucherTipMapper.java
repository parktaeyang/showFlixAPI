package com.showflix.api.schedule.mapper;

import com.showflix.api.schedule.domain.VoucherTip;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Infrastructure Layer - VoucherTip MyBatis Mapper
 */
@Mapper
public interface VoucherTipMapper {

    List<VoucherTip> findByDate(@Param("date") String date);

    void upsert(VoucherTip voucherTip);
}
