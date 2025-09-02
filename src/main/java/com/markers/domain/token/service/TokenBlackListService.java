package com.markers.domain.token.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenBlackListService {
    private final StringRedisTemplate redisTemplate;

    public void addRefreshTokenBlackList(String refreshToken, long expiration){
        redisTemplate.opsForValue().set("blacklist:refresh:" + refreshToken, "1", Duration.ofSeconds(expiration));
    }

    public boolean isAlreadyBlackListed(String refreshToken){
        Optional<String> refreshOpt = Optional.ofNullable(redisTemplate.opsForValue().get("blacklist:refresh:" + refreshToken));
        return refreshOpt.isPresent();
    }

}
