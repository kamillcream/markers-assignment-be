package com.markers.global.jwt;

import com.markers.domain.token.service.TokenBlackListService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

import static com.markers.global.jwt.CookieUtil.expireCookie;

@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final TokenBlackListService tokenBlackListService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        Optional<String> accessTokenOpt = CookieUtil.getAccessTokenFromRequest(request);
        Optional<String> refreshTokenOpt = CookieUtil.getRefreshTokenFromRequest(request);

        if (refreshTokenOpt.isPresent()) {
            if (tokenBlackListService.isAlreadyBlackListed(refreshTokenOpt.get())){
                expireCookie(response, "refresh", "/", true, true, "None");
                expireCookie(response, "access", "/", true, true, "None");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "이미 로그아웃한 사용자의 토큰입니다.");
                return;
            }
        }


        if (accessTokenOpt.isPresent()) {
            String token = accessTokenOpt.get();
            if (!jwtUtil.isExpired(token)) {
                String email = jwtUtil.getEmail(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                log.debug("❌ JWT 토큰 검증 실패");
            }
        } else {
            log.debug("❌ access 토큰 없음, URI={}", request.getRequestURI());
        }
        filterChain.doFilter(request, response);
    }
}
