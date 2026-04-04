package com.showflix.api.admin.mapper;

import com.showflix.api.admin.domain.DailyStatusEntry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DailyStatusMapper {

    List<DailyStatusEntry> findByDate(@Param("date") String date);
}
