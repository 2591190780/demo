package com.example.utils;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class FlowUtils {
    @Resource
    StringRedisTemplate stringRedisTemplate;

    public boolean limitOnceCheck(String key,int blockTime){
        if(Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))){
            return false;
        }else{
            stringRedisTemplate.opsForValue().set(key,"",blockTime, TimeUnit.SECONDS);
            return true;
        }

    }
}
