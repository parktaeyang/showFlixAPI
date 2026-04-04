package com.showflix.api.admin.mapper;

import com.showflix.api.admin.domain.XmasSeat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface XmasSeatMapper {

    List<XmasSeat> findByEventDate(@Param("eventDate") String eventDate);

    XmasSeat findById(@Param("id") Long id);

    void insert(XmasSeat xmasSeat);

    void update(XmasSeat xmasSeat);

    void deleteById(@Param("id") Long id);
}
