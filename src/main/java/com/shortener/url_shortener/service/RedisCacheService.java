package com.shortener.url_shortener.service;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisCacheService {

    private final StringRedisTemplate redisTemplate;
    private static final Duration DEFAULT_TTL = Duration.ofHours(1);
    private static final String LINK_PREFIX = "links::";

    public RedisCacheService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String getOriginalUrl(String code) {
        return redisTemplate.opsForValue().get(LINK_PREFIX + code);
    }

    public void cacheOriginalUrl(String code, String originalUrl) {
        redisTemplate.opsForValue().set(LINK_PREFIX + code, originalUrl, DEFAULT_TTL);
    }

    public void evict(String code) {
        redisTemplate.delete(LINK_PREFIX + code);
    }
}
