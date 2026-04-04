package com.showflix.api.admin.mapper;

import com.showflix.api.admin.domain.HealthCert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HealthCertMapper {

    List<HealthCert> findAllWithUserName();

    void upsert(HealthCert healthCert);
}
