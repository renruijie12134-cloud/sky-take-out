package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    /**
     * 新增菜品和口味
     * @param dishDTO
     * @return
     * 因为我要操作两张表，所以最好打开注解
     */
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        // 根据业务逻辑：
        // 我们可以一次向菜品表中插入1条数据，向口味表中插入多条数据

        // 1:新增菜品
        // 你不能把DishDTO对象传到dish表里
        // 你的传 dish表对应的实体类Dish对象
        Dish dish = new Dish();
        // 对象的复制
        BeanUtils.copyProperties(dishDTO, dish);

        // 其余为复制上的字段，创建者，创建时间，更新者，更新时间
        // 这些不用你写，AOP会接手
        dishMapper.insert(dish);

        // 获取inset语句生成的主键值
        // 因为我们现在是插入数据，数据没入数据库前没有id
        // 经过 dishMapper.insert(dish); 后，数据入库了，它就有Id了
        // 我们可以在 XML 语句中写 useGeneratedKeys="true" keyProperty="id"
        // 标签来设置数据库插入数据是自增iD，并将ID赋给实体类对象dish的id字段
        // 此后我们就可以通过dish.getId()来获取到插入数据的id了
        Long dish_id = dish.getId();

        // 2:新增口味
        // 先把口味数据取出来
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            // 给所有数据的dishid 赋值
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dish_id));
            // 一次性插入多条数据
            dishFlavorMapper.insertBatch(flavors);

        }



    }
}
