package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Slf4j
@Api(tags="店铺状态")
public class ShopController {

    private static final String KEY = "SHOP_STATUS";
    // 注入redis bean
    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 用户端查询业务状态
     */
    @GetMapping("/status")
    @ApiOperation("用户端查询业务状态")
    public Result<Integer> userGetShopStatus() {
        Integer shopStatus = (Integer)redisTemplate.opsForValue().get(KEY);
        log.info("用户端查询店铺营业状态为：{}", shopStatus==1 ?"营业中":"打烊中");
        return Result.success(shopStatus);
    }

}

