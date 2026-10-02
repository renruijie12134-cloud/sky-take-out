package com.sky.controller.admin;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 *菜品管理
 */

@RestController
@RequestMapping("/admin/dish")
@Api(tags="菜品相关接口")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     *
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品：{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
        // 清理redis缓存
        String key="dish_"+dishDTO.getCategoryId();
//        redisTemplate.delete(key);
        clearCache(key);
        return Result.success();

    }

    /**
     * 菜品分页查询
     * @parm dishPageQueryDTO
     * @return
     * @return
     * GetMapping从前端获取的参数可以不用 @RequestBody修饰，
     * 而从Postman获取的参数需要使用 @RequestBody修饰
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品查询分页：{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 菜品的批量删除
     * @param ids
     * @return
     * 前端传来的是一个字符串，包含多个id，用逗号分隔，
     * 利用SpringMVC的自动参数转换，将字符串转换成List<Long>再传给服务层
     * 参数转换是自动进行的，@RequestParam，@PathVariable都具备这个功能
     */
    @DeleteMapping
    @ApiOperation("菜品的批量删除")
    public Result delete(@RequestParam List<Long> ids){
        log.info("菜品的批量删除{}", ids);
        dishService.deleteBatch(ids);
        // 清理redis缓存，因为批量删除时，或许修改多个分类下的菜品
        // 我无法区分到时修了哪一个，或许可以用一段复杂的代码找出到底需要清理那个分类
        // 但是太麻烦，所以我选择将所有分类下的菜品缓存都清理掉
//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);
        clearCache("dish_*");

        return Result.success();

    }

    /**
     * 根据id查询菜品和对应的口味数据
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("根据id查询菜品：{}", id);
        DishVO dishVO = dishService.getById(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品：{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);

        // 在你修改时，有些字段就算你修改了，也不需要更新缓存，因为这些字段不涉及菜品的查询
        // 但有些就影响，比如修改菜品的分类，修改菜品的状态（起售和停售），这些操作都需要更新缓存
        // 为了方便，缓存全删掉
//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);
        clearCache("dish_*");


        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(@RequestParam Long categoryId){
        log.info("根据分类id查询菜品：{}", categoryId);
        List<Dish> dishList = dishService.list(categoryId);
        return Result.success(dishList);
    }

    /**
     * 清流缓存数据
     */
    private void clearCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }

//    /**
//     * 菜品的起售和停售
//     */
//    @PostMapping("/status/{status}")
//    @ApiOperation("菜品的起售和停售")
//    public Result updateStatus(@PathVariable Integer status, @RequestParam Long id){
//        log.info("菜品的起售和停售：{}", status);
//        dishService.updateStatus(status, id);

//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);

//        return Result.success();
//    }

}
