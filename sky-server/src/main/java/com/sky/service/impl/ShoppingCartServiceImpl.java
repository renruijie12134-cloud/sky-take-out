package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {


    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;


    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车，商品信息：{}", shoppingCartDTO);
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);

        // 1:判断当前加入购物车的商品是否已经存在
        if (shoppingCartList != null && shoppingCartList.size() > 0) {
            // 如果存在，则数量加一
            ShoppingCart existShoppingCart = shoppingCartList.get(0);
            existShoppingCart.setNumber(existShoppingCart.getNumber() + 1);
            shoppingCartMapper.updateById(existShoppingCart);
        } else {
            // 如果不存在，则直接加入购物车
            // 判断是添加菜品好还是添加套餐

            Long dishId = shoppingCartDTO.getDishId();
            Long setmealId = shoppingCartDTO.getSetmealId();
            if (dishId != null) {
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            } else {

                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }


    }

    /**
     * 查看购物车
     */
    public List<ShoppingCart> list() {
        log.info("查看购物车");
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        return shoppingCartMapper.list(shoppingCart);
    }

    /**
     * 清空购物车
     */
    public void clean() {
        log.info("清空购物车");
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(BaseContext.getCurrentId()).build();
        shoppingCartMapper.cleanShoppingCard(shoppingCart);
    }

    /**
     * 删除购物车中的一个
     */
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        log.info("删除购物车中的一个，商品信息：{}", shoppingCartDTO);
        // 先把要删除的商品取出来
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);

        // 判断购物车中是否存在该商品
        if (shoppingCartList != null && shoppingCartList.size() > 0) {
            ShoppingCart existShoppingCart = shoppingCartList.get(0);
            // 看购买的数量是否大于1
            // 如果大于1，则数量减一
            if (existShoppingCart.getNumber() > 1) {
                existShoppingCart.setNumber(existShoppingCart.getNumber() - 1);
                shoppingCartMapper.updateById(existShoppingCart);
            } else {
                // 如果等于1，则直接删除
                existShoppingCart.setUserId(BaseContext.getCurrentId());
                shoppingCartMapper.cleanShoppingCard(existShoppingCart);
            }
        }
    }



}
