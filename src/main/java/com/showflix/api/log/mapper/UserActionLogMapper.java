package com.showflix.api.log.mapper;

import com.showflix.api.log.domain.UserActionLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserActionLogMapper {

    void insert(UserActionLog log);

    List<UserActionLog> findByFilter(@Param("username") String username,
                                     @Param("action") String action,
                                     @Param("startDate") String startDate,
                                     @Param("endDate") String endDate,
                                     @Param("offset") int offset,
                                     @Param("size") int size);

    int countByFilter(@Param("username") String username,
                      @Param("action") String action,
                      @Param("startDate") String startDate,
                      @Param("endDate") String endDate);

    int deleteOlderThan(@Param("cutoffDate") String cutoffDate);
}
