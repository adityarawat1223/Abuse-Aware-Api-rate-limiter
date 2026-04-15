package com.apigateway.middleware;

import com.apigateway.dto.ClientInfo;
import com.apigateway.dto.Response;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class ApiMiddleware {
    private final StringRedisTemplate redisTemplate;
    public ApiMiddleware(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    public Response isratelimited(ClientInfo client){

        String key = "rl" + client.Clientip + ":" + client.Apikey;

        Long count = redisTemplate.opsForValue().increment(key);
        Response response = new Response();
        if(count != null && count == 1 ){
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }

        response.Response_Id = (count != null && count > 10) ? 429 : 200;
        response.Rate_Limit_Limit = 10;
        response.Rate_Limit_Usage = (count == null ? 0 : count);
        Long ttl = redisTemplate.getExpire(key);
        response.UntilExpiration = (ttl == null || ttl <= 0 ? 0 : ttl);
        return response;

    }
}
