package com.markers.domain.user.service;

import com.markers.domain.user.dto.LoginRequest;
import com.markers.domain.user.dto.RegisterRequest;
import com.markers.domain.user.dto.UserInfoResponse;
import com.markers.domain.user.entity.User;
import com.markers.domain.user.repository.UserRepository;
import com.markers.global.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    public UserInfoResponse fetchUserInfo(CustomUserDetails customUserDetails){
        return new UserInfoResponse(customUserDetails.getUsername(), customUserDetails.getEmail());
    }

    @Transactional
    public void insertNewUser(RegisterRequest registerRequest){
        User newUser = User.builder()
                .name(registerRequest.name())
                .email(registerRequest.email())
                .password(encoder.encode(registerRequest.password()))
                .build();

        userRepository.save(newUser);
    }
}
