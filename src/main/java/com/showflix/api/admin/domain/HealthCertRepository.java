package com.showflix.api.admin.domain;

import java.util.List;

/**
 * Domain Layer - 보건증 관리 저장소 Port
 */
public interface HealthCertRepository {

    List<HealthCert> findAllWithUserName();

    void upsert(HealthCert healthCert);
}
