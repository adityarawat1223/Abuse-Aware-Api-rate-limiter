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
    public Response IsRateLimited(ClientInfo client){

        String key = "rl:" + client.Apikey + ":" + client.ClientIp;
        String penaltyKey = "penalty:" + client.Apikey + ":" + client.ClientIp;
        Response response = new Response();
        long now = Instant.now().toEpochMilli();
        String WindowInfo = redisTemplate.opsForValue().get(penaltyKey);
        long Window = (WindowInfo == null ? 60000 : Long.parseLong(WindowInfo));

        redisTemplate.opsForZSet().removeRangeByScore(key,0,now - Window);
        Long count = redisTemplate.opsForZSet().count(key, now - Window, now);
        count = (count == null ? 0 : count);

        if( count >= 10){
            response.Response_Id = 429;
            Window = Math.min(Window * 2, 3600000L);
            response.ChangeInfo = "Your Token Refill time is Increased ,Stop Being Bad";
            redisTemplate.opsForValue().set(penaltyKey,String.valueOf(Window) , java.time.Duration.ofMillis(Window));
        }

        else {
            response.Response_Id = 200;

            if (count < 5 && Window > 60000) {
                Window = Math.max(60000, Window / 2);
                response.ChangeInfo = "Your Token Refill time is Decrease Due to Good Behaviour";
                redisTemplate.opsForValue().set(
                        penaltyKey,
                        String.valueOf(Window),
                        java.time.Duration.ofMillis(Window)
                );
            }
            else{
                response.ChangeInfo = "Your Token Refill time is Unchanged";

            }
            redisTemplate.opsForZSet().add(key, UUID.randomUUID().toString(), now);
            count++;
        }

        response.Rate_Limit_Usage = count;
        response.Rate_Limit_Limit =  10;
        response.UntilExpiration = untilExpiration(key , Window);
        redisTemplate.expire(key, java.time.Duration.ofMillis(Window + 5000));
        return response;
    }
}
