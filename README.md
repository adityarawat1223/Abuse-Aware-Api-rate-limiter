# Abuse-Aware-Api-rate-limiter

A high-performance, distributed rate-limiting middleware built with Spring Boot and Redis. Unlike static limiters, this system utilizes a Dynamic Sliding Window that adapts to user behavior—penalizing aggressive actors with exponential backoff while rewarding "good citizens" with window recovery.
