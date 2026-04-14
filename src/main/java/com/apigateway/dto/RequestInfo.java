package com.apigateway.dto;

import java.util.concurrent.ConcurrentLinkedQueue;

public class RequestInfo
{
    public ConcurrentLinkedQueue<Long> timestamps;
    public Integer Counter = 0;
    public long Blockeduntil = 0;
}
