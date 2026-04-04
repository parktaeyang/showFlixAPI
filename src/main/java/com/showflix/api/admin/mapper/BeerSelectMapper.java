package com.showflix.api.admin.mapper;

import com.showflix.api.admin.domain.BeerSelect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BeerSelectMapper {

    List<BeerSelect> findAll();

    BeerSelect findById(@Param("id") Long id);

    void insert(BeerSelect beerSelect);

    void update(BeerSelect beerSelect);

    void deleteById(@Param("id") Long id);
}
