package com.markers.domain.user.controller;

import com.markers.domain.user.dto.LoginRequest;
import com.markers.domain.user.dto.RegisterRequest;
import com.markers.domain.user.dto.UserInfoResponse;
import com.markers.domain.user.service.UserService;
import com.markers.global.auth.CustomUserDetails;
import com.markers.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        return ResponseEntity.ok(ApiResponse.onSuccess(userService.fetchUserInfo(customUserDetails)));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> postRegister(@Valid @RequestBody RegisterRequest registerRequest){
        userService.insertNewUser(registerRequest);
        return ResponseEntity.ok(ApiResponse.onSuccessVoid());
    }
}
