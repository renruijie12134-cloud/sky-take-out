package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
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

    @Autowired
    private SetmealDishMapper setmealDishMapper;

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

    /**
     *  菜品分页查询
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());

        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 菜品的批量删除
     * @param ids
     * @return
     *
     */
    @Transactional
    public void deleteBatch(List<Long> ids){
        // 起售中的菜品不可以被删除
        // 被套餐关联的菜品不可以被删除
        // 删除菜品后，关联的口味也要删除掉

        // 1：判断当前商品是否可以被删除----是否起售，
        // dish表：status int	售卖状态	1起售 0停售
        // StatusConstant.ENABLE=1   MessageConstant.DISH_ON_SALE="起售中的菜品不能删除"
        for (Long id : ids){
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus().equals(StatusConstant.ENABLE)) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        // 2：判断当前商品是否可以被删除----是否被套餐关联
        // setmeal表为套餐表
        // 思路根据菜品id查询套餐id，如果查得到，那就不能删-----你可以换思路，，這就是人與人的區別
        List<Long> setmealIds = setmealDishMapper.getSetmealByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }


        // 3：删除菜品表中的数据
        dishMapper.deleteBatchByIds(ids);
        // 4：删除菜品对应口味表中的数据
        dishFlavorMapper.deleteBatchByDishIds(ids);

    }


    /**
     * 根据id查询菜品和对应的口味数据
     */
    public DishVO getById(Long id) {
        // 1：根据id查询菜品数据
        Dish dish = dishMapper.getById(id);
        // 2：根据菜品id查询口味数据
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);
        // 3：将上面的两个数据封装成DishVO对象返回
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    /**
     * 修改菜品
     */
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO){
        // 1：先根据id修改菜品数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.updateById(dish);
        // 2：再根据id删除口味数据
        dishFlavorMapper.deleteById(dishDTO.getId());
        // 3：再根据id添加口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            // 给所有数据的dishid 赋值
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishDTO.getId()));
            // 一次性插入多条数据
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    public List<Dish> list(Long categoryId) {
        // 只找这个分类下正在起售的菜品。
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }


}
