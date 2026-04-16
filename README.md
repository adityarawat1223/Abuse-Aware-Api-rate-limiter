# Abuse-Aware-API-Rate-Limiter

[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)](https://spring.io/projects/spring-boot)
[![Redis](https://img.shields.io/badge/Redis-7.x-red)](https://redis.io/)

## Overview 
This is Plug-n-Play Abuse Aware Api rate Limiter
. This is a Generalised Version As This could work
for all kind of API without any Specific Changes

##  System Design
### **The Sliding Window Log (ZSet)**
Unlike "Fixed Window" or "Token Bucket" algorithms, this project uses **Redis Sorted Sets**. Every request is stored as a unique member with a millisecond timestamp as its score.
* **Cleanup:** `ZREMRANGEBYSCORE` removes timestamps older than the current dynamic window.
* **Check:** `ZCARD` provides an $O(Logn)$ count of active requests within the window.

### **Penalty Algorithm**
The system monitors 429 triggers to calculate a **Penalty Multiplier**:
1.  **On Violation:** `Window = Math.min(Window * 2, Max_Penalty)`
2.  **On Recovery:** If `usage < (Limit / 2)`, `Window = Math.max(Base_Window, Window / 2)`
### **Response**
```JSON
{
  "ChangeInfo": "Your Token Refill time is Unchanged",
  "Rate_Limit_Limit": 10,
  "Rate_Limit_Usage": 1,
  "Response_Id": 200,
  "UntilExpiration": 59
}
```
## Installation and Setup
### Prerequisites 
1. Java 17 
2. Redis 

### Setup
1. **Run Redis:** On Ubuntu/WSL ```run sudo service redis-server start```.

2. **Start the App**: Run through your IDE or .```/mvnw spring-boot:run```.

Test it: Hit the API 11 times and watch the UntilExpiration and ChangeInfo react.
