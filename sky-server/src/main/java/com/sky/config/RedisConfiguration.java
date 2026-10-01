package com.sky.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j

public class RedisConfiguration {
    // 配置RedisTemplate
    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("开始创建redis模板：RedisTemplate");
        RedisTemplate redisTemplate = new RedisTemplate();
        // 设置redis连接工厂对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        /**
         * 下面的这些序列器材都是要设置的，
         * 1. key的序列化器
         * 2. value的序列化器
         * 3. hashKey的序列化器
         * 4. hashValue的序列化器
         * 5. 其他序列化器
         * 如果不设置的话，默认使用JdkSerializationRedisSerializer
         * 这个序列化器在存储数据的时候会将key和value都转换为byte数组再存储
         * 但是Redis存储的key和value都是字符串，默认的序列化方式会导致一堆乱码
         * 所以这里需要设置成StringRedisSerializer
         */

        // 设置key的序列化器，默认为 JdkSerializationRedisSerializer
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // 设置value的序列化器，默认为 JdkSerializationRedisSerializer
//        redisTemplate.setValueSerializer(new StringRedisSerializer());
//        // 设置hashKey的序列化器，默认为 JdkSerializationRedisSerializer
//        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
//        // 设置hashValue的序列化器，默认为 JdkSerializationRedisSerializer
//        redisTemplate.setHashValueSerializer(new StringRedisSerializer());

        return redisTemplate;
    }
}
