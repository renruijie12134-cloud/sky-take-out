package com.sky.mapper;


import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 查询购物车
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 根据id更新购物车数量
     * @param shoppingCart
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateById(ShoppingCart shoppingCart);


    /**
     * 插入购物车数据
     */
    @Insert("insert into shopping_cart (user_id, dish_id, setmeal_id, name, amount, image, number, dish_Flavor,create_time) " +
            "values (#{userId}, #{dishId}, #{setmealId}, #{name}, #{amount}, #{image}, #{number}, #{dishFlavor},#{createTime})")
    void insert(ShoppingCart shoppingCart);

    /**
     * 删除购物车数据,根据对象删除，可以删除一个，也可以上删除一堆
     */
    void cleanShoppingCard(ShoppingCart shoppingCart);

}
