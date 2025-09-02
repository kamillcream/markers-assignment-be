package com.markers.global.response.exception;

import com.markers.global.response.BaseStatusCode;
import com.markers.global.response.ErrorDetail;
import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException {

    private final BaseStatusCode code;

    public GlobalException(BaseStatusCode code) {
        super(code.getMessage());
        this.code = code;
    }

    public ErrorDetail getErrorDetail() {
        return ErrorDetail.from(this.code);
    }

}