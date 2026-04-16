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
    private Long untilExpiration( String key , long window) {

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


        return  Math.max(0L, (long)(score +  window - now) / 1000);
    }
    public Response isratelimited(ClientInfo client){

        String key = "rl:" + client.Apikey + ":" + client.Clientip;
        String penaltyKey = "penalty:" + client.Apikey + ":" + client.Clientip;
        Response response = new Response();
        long now = Instant.now().toEpochMilli();
        String windowsize = redisTemplate.opsForValue().get(penaltyKey);

        long window = (windowsize == null ? 60000 : Long.parseLong(windowsize));

        redisTemplate.opsForZSet().removeRangeByScore(key,0,now - window);
        Long count = redisTemplate.opsForZSet().count(key, now - window, now);
        count = (count == null ? 0 : count);

        if( count >= 10){
            response.Response_Id = 429;
            window = Math.min(window * 2, 3600000L);
            redisTemplate.opsForValue().set(penaltyKey,String.valueOf(window) , java.time.Duration.ofMillis(window));
        }

        else {
            response.Response_Id = 200;

            if (count < 5 && window > 60000) {
                window = Math.max(60000, window / 2);

                redisTemplate.opsForValue().set(
                        penaltyKey,
                        String.valueOf(window),
                        java.time.Duration.ofMillis(window)
                );
            }

            redisTemplate.opsForZSet().add(key, UUID.randomUUID().toString(), now);
            count++;
        }

        response.Rate_Limit_Usage = count;
        response.Rate_Limit_Limit =  10;
        response.UntilExpiration = untilExpiration(key , window);
        redisTemplate.expire(key, java.time.Duration.ofMillis(window + 5000));
        return response;

    }
}
