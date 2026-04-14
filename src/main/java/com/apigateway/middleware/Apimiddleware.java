package com.apigateway.middleware;

import com.apigateway.dto.ClientInfo;
import com.apigateway.dto.RequestInfo;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class Apimiddleware  {
    private  final ConcurrentHashMap<String,RequestInfo> Map = new ConcurrentHashMap<>();
    public boolean isratelimited(ClientInfo client){

        boolean is = false;
        String key = client.Clientip + client.Apikey;
        if(Map.containsKey(key)){

            RequestInfo reqinfo = Map.get(key);
            long millis = java.time.Instant.now().toEpochMilli();
            if(reqinfo.Blockeduntil >= millis){
                if(reqinfo.Blockeduntil > millis){
                    is = true;
                }
                else{
                    reqinfo.Blockeduntil = 0;
                    reqinfo.Counter= 0;
                }
            }
            else if(reqinfo.Counter < 3){
                reqinfo.Counter++;
            }

            else{

                while(!reqinfo.timestamps.isEmpty() && reqinfo.timestamps.peek() + 60000 <= millis){
                    reqinfo.timestamps.poll();
                }

                if(reqinfo.timestamps.size() == 5){
                    reqinfo.Blockeduntil = millis + (60 * 1000);
                    is =  true;
                }

                else{
                    reqinfo.timestamps.add(millis);
                }

            }
        }
        else{

            RequestInfo reqinfo = new RequestInfo();
            reqinfo.Counter++;
            reqinfo.timestamps = new ConcurrentLinkedQueue<>();
            Map.put(key,reqinfo);
        }
        return is;
    }
}
