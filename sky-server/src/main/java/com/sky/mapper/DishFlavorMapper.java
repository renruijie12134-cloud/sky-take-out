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

    /**
     * 批量新增口味
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据菜品ID批量删除口味
     * @param  ids
     */
    void deleteBatchByDishIds(List<Long> ids);
}
