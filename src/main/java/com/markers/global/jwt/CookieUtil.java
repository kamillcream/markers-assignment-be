package com.markers.global.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;

import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieUtil {
    private static final int COOKIE_EXPIRE_TIME = 30 * 60; // 30분

    public Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setPath("/");
        cookie.setMaxAge(COOKIE_EXPIRE_TIME);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);  // HTTPS 요청에만 secure 설정
        cookie.setAttribute("SameSite", "Strict");
        return cookie;
    }

    public ResponseCookie createResponseCookie(String refreshToken){
        return ResponseCookie.from("refresh", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict") // CSRF 방어
                .path("/")      // 모든 경로에서 유효
                .maxAge(COOKIE_EXPIRE_TIME) // 7일 유지
                .build();
    }

    public static void expireCookie(
            HttpServletResponse res, String name, String path,
            boolean httpOnly, boolean secure, String sameSite
    ) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath(path);
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(secure);
        cookie.setMaxAge(0);

        cookie.setAttribute("SameSite", sameSite);
        res.addCookie(cookie);
    }


    public static Optional<String> getAccessTokenFromRequest(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> "access".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    public static Optional<String> getRefreshTokenFromRequest(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> "refresh".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
