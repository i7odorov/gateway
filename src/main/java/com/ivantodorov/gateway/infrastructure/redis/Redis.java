package com.ivantodorov.gateway.infrastructure.redis;

import com.ivantodorov.gateway.domain.exception.CustomRedisException;
import com.ivantodorov.gateway.domain.model.properties.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class Redis {

    private static final String PREFIX = "req:";

    private final StringRedisTemplate redisTemplate;
    private final AppProperties appProperties;

    public boolean isDuplicate(String serviceId, String requestId) {

        try {

            return redisTemplate.hasKey(buildKey(serviceId, requestId));
        } catch (Exception e) {

            log.error("Error checking for duplicate in Redis for requestId: {} and serviceId: {}", requestId, serviceId);
            throw new CustomRedisException("Error checking for duplicate in Redis");
        }
    }

    public void markAsProcessed(String serviceId, String requestId) {

        redisTemplate.opsForValue().set(buildKey(serviceId, requestId), "1", appProperties.getRedisTtlHours());

        try {

            redisTemplate.opsForValue().set(buildKey(serviceId, requestId), "1", appProperties.getRedisTtlHours());
        } catch (Exception e) {

            log.error("Error marking request as processed in Redis for requestId: {} and serviceId: {}", requestId, serviceId, e);
            throw new CustomRedisException("Error marking request as processed in Redis");
        }
    }

    private String buildKey(String serviceId, String requestId) {

        return PREFIX + serviceId + ":" + requestId;
    }
}