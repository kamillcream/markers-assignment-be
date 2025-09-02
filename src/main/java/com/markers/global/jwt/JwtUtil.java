package com.markers.global.jwt;

import com.markers.domain.user.entity.User;
import com.markers.domain.user.repository.UserRepository;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class JwtUtil {

    private static final long ACCESS_TOKEN_EXPIRE_TIME = 15 * 60 * 1000; // 30분
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 60 * 60 * 1000; // 1시

    private SecretKey secretKey;
    private final UserRepository userRepository;

    //검증 메서드

    public JwtUtil(@Value("${jwt.secret}") String secret, UserRepository userRepository) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.userRepository = userRepository;
    }

    public String getEmail(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                .get("email", String.class);
    }

    public String getCategory(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                .get("category", String.class);
    }

    public Boolean isExpired(String token) {
        try {

            Claims claims = extractClaims(token);
            Date expiration = claims.getExpiration();

            return expiration.before(new Date());

        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public String createAccess(String email) {
        return Jwts.builder()
                .claim("category", "access")
                .claim("email", email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE_TIME))
                .signWith(secretKey)
                .compact();
    }

    public String createRefresh(String email) {
        return Jwts.builder()
                .claim("category", "refresh")
                .claim("email", email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(secretKey)
                .compact();
    }

    public long getRefreshTokenExpireTime(String refreshToken) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();

            Date expiration = claims.getExpiration();
            if (expiration == null) return 0L;

            Instant exp = expiration.toInstant();
            return Math.max(Duration.between(Instant.now(), exp).getSeconds(), 0);
        } catch (Exception e) {
            return 0L;
        }
    }

    private Claims extractClaims(String token){
        return Jwts.parser()
                .setSigningKey(secretKey)
                .build()
                .parseSignedClaims(token)
                .getBody();

    }

    public Authentication getAuthentication(String token) {
        String email = getEmail(token);

        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("JWT token does not contain a valid email.");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }


        User user = userOptional.get();
        List<GrantedAuthority> authorities = new ArrayList<>();

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(), "", authorities
        );

        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }
}
