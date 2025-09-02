package com.markers.global.response.status;

import com.markers.global.response.BaseStatusCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseStatusCode {

    // 공통 응답
    _OK(HttpStatus.OK, "COMMON_200", "성공입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}