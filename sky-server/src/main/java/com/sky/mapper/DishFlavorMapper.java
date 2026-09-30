package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 新增口味
     * @param dishFlavor
     */

    @Insert("insert into dish_flavor (dish_id, flavor_name, flavor_value) values (#{dishId}, #{flavorName}, #{flavorValue})")
    void insert(DishFlavor dishFlavor);

    void insertBatch(List<DishFlavor> flavors);
}
