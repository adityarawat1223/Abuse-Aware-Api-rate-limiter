package com.apigateway.middleware;

import com.apigateway.dto.ClientInfo;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class ApiMiddleware {
    private final StringRedisTemplate redisTemplate;
    public ApiMiddleware(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    public boolean isratelimited(ClientInfo client){

        String key = "rl:" + client.Clientip + "tp" + client.Apikey;

        Long count = redisTemplate.opsForValue().increment(key);

        if(count != null && count == 1 ){
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }
        return count != null && count > 10;

    }
}
