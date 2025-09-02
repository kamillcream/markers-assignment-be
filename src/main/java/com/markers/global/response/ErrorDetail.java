package com.markers.global.response;

import org.springframework.http.HttpStatus;

public record ErrorDetail(
        HttpStatus httpStatus,
        String code,
        String message) {

        public static ErrorDetail from(BaseStatusCode code) {
            return new ErrorDetail(
                    code.getHttpStatus(),
                    code.getCode(),
                    code.getMessage()
            );
        }
}