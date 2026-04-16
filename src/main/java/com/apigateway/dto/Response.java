package com.apigateway.dto;

public class Response {
    public Integer Rate_Limit_Limit; // Standard limit
    public Long Rate_Limit_Usage;    // Match the Redis type
    public Integer Response_Id;
    public Long UntilExpiration;
    public String ChangeInfo;
}