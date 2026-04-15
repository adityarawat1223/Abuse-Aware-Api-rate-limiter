package com.apigateway.controllers;

import com.apigateway.dto.*;
import com.apigateway.middleware.ApiMiddleware;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProductInfo {

    private final ApiMiddleware rateLimiter;

    public ProductInfo(ApiMiddleware rateLimiter) {
        this.rateLimiter = rateLimiter;
    }


    @GetMapping("/product")
    public Response Apicontroller(HttpServletRequest request) {

        Response answer = new Response();

        ClientInfo Client = new ClientInfo();
        Client.Clientip = request.getRemoteAddr();
        Client.Apikey = "Product";

        if(rateLimiter.isratelimited(Client)){

            answer.Rpid = 429;
        }

        else{
            answer.Rpid = 200;
        }

        return answer;
    }
}