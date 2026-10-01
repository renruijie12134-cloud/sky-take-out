package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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

    /**
     * 根据单个id删除口味
     */
    @Delete("delete from dish_flavor where id = #{id}")
    void deleteById(Long id);

    /**
     * 根据菜品ID查询口味数据
     */
    @Select("select * from dish_flavor where dish_id = #{id}")
    List<DishFlavor> getByDishId(Long id);
}
