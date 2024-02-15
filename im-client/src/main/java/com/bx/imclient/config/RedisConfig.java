//package com.bx.imclient.config;
//
//import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
//@Configuration("IMRedisConfig")
//public class RedisConfig {
//
//    @Bean("IMRedisTemplate")
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
//        RedisTemplate<String, Object> redisTemplate = new RedisTemplate();
//        redisTemplate.setConnectionFactory(redisConnectionFactory);
//        // 设置值（value）的序列化采用FastJsonRedisSerializer
//        redisTemplate.setValueSerializer(fastJsonRedisSerializer());
//        redisTemplate.setHashValueSerializer(fastJsonRedisSerializer());
//        // 设置键（key）的序列化采用StringRedisSerializer。
//        redisTemplate.setKeySerializer(new StringRedisSerializer());
//        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
//        redisTemplate.afterPropertiesSet();
//        return redisTemplate;
//    }
//
//    public FastJsonRedisSerializer fastJsonRedisSerializer(){
//        return new FastJsonRedisSerializer<>(Object.class);
//    }
//
//}
