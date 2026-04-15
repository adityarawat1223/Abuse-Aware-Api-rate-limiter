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


        ClientInfo Client = new ClientInfo();
        Client.Clientip = request.getRemoteAddr();
        Client.Apikey = "Product";


        return rateLimiter.isratelimited(Client);
    }
}