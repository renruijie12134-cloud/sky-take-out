package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import io.swagger.v3.oas.annotations.servers.ServerVariable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WeChatProperties weChatProperties;

    // 微信服务接口地址
    private static final String WX_SERVICE_URL = "https://api.weixin.qq.com/sns/jscode2session";


    /**
     * 微信用户登录
     */
    public User wxLogin(UserLoginDTO userLoginDTO) {
        // 获取openid
        String openid = getOpenid(userLoginDTO.getCode());

        //判断openid是否为空，如果为空表示登录失败，抛出业务异常
        if (openid == null) {
            log.error("微信登录失败，openid为空");
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        //判断当前用户是否为新用户
        User user = userMapper.getByOpenid(openid);

        // 如果是新用户，自动完成注册
        if (user == null) {
            user = User.builder().openid(openid).createTime(LocalDateTime.now()).build();
            userMapper.insert(user);
        }

        return user;
    }

    /**
     *  调用微信接口服务，根据code获取openid
     */
    private String getOpenid(String code) {
        //调用微信接口服务，获得当前微信用户Openid
        Map<String, String> map = new HashMap<>();
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
        // result中就存放了当前微信用户的所有信息，但是它是一个json字符串，需要我们手动解析
        String result = HttpClientUtil.doGet(WX_SERVICE_URL, map);
        // 解析result 为json对象
        JSONObject jsonObject = JSON.parseObject(result);
        // jsonObject中就存放了当前微信用户的所有信息，但是它是一个json对象，可以通过get set方法访问其中的属性
        String openid = jsonObject.getString("openid");
        return openid;
    }

}
