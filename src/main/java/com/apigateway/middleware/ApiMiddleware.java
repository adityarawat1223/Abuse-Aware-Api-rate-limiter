package com.apigateway.middleware;

import com.apigateway.dto.ClientInfo;
import com.apigateway.dto.Response;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;


@Component
public class ApiMiddleware {

    private final StringRedisTemplate redisTemplate;
    public ApiMiddleware(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    private Long untilExpiration( String key) {

        Set<ZSetOperations.TypedTuple<String>> set =
                redisTemplate.opsForZSet().rangeWithScores(key, 0, 0);

        if (set == null || set.isEmpty()) {
            return 0L;
        }

        ZSetOperations.TypedTuple<String> tuple = set.iterator().next();
        Double score = tuple.getScore();

        if (score == null) {
            return 0L;
        }

        long now = Instant.now().toEpochMilli();


        return  Math.max(0L, (long)(score +  60000 - now) / 1000);
    }
    public Response isratelimited(ClientInfo client){

        String key = "rl:" + client.Apikey + ":" + client.Clientip;
        Response response = new Response();
        long now = Instant.now().toEpochMilli();
        long window = now - 60000;

        redisTemplate.opsForZSet().removeRangeByScore(key,0,window);
        Long count = redisTemplate.opsForZSet().count(key, window, now);
        count = (count == null ? 0 : count);

        if( count >= 10){
            response.Response_Id = 429;
        }

        else{
            response.Response_Id = 200;
            UUID uuid = UUID.randomUUID();
            String unique = uuid.toString();
            redisTemplate.opsForZSet().add(key, unique,now);
            count++;

        }

        response.Rate_Limit_Usage = count;
        response.Rate_Limit_Limit =  10;
        response.UntilExpiration = untilExpiration(key);
        redisTemplate.expire(key, java.time.Duration.ofMinutes(1));
        return response;

    }
}
