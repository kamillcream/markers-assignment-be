package com.markers.global.response.exception;

import com.markers.global.response.ApiResponse;
import com.markers.global.response.ErrorDetail;
import com.markers.global.response.status.ErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RestControllerAdvice(annotations = {RestController.class})
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

    // Bean Validation 제약 조건 위반 시 발생하는 예외를 처리
    @ExceptionHandler
    public ResponseEntity<Object> validation(ConstraintViolationException e, WebRequest request) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(violation -> String.format("prop '%s' | val '%s' | msg %s",
                        violation.getPropertyPath(), // 위반된 필드 경로
                        violation.getInvalidValue(), // 유효하지 않은 값
                        violation.getMessage()       // 제약 조건 위반 메시지
                ))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("ConstraintViolationException 추출 도중 에러 발생"));

        return handleExceptionInternalConstraint(e, HttpHeaders.EMPTY, request, errorMessage);
    }

    // @Valid 어노테이션을 통한 검증 실패 시 발생하는 예외를 처리
    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status,
            WebRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();

        e.getBindingResult().getFieldErrors()
                .forEach(fieldError -> {
                    String fieldName = fieldError.getField();
                    String errorMessage = Optional.ofNullable(fieldError.getDefaultMessage())
                            .orElse("");
                    errors.merge(fieldName, errorMessage,
                            (existingErrorMessage, newErrorMessage) -> existingErrorMessage + ", "
                                    + newErrorMessage);
                });

        return handleExceptionInternalArgs(e, HttpHeaders.EMPTY,
                ErrorStatus.valueOf("_BAD_REQUEST"), request, errors);
    }

    // 모든 Exception 클래스 타입의 예외 처리 (500번대)
    @ExceptionHandler
    public ResponseEntity<Object> exception(Exception e, WebRequest request) {

        String errorMessage = e.getMessage();
        String errorPoint = Objects.isNull(e.getStackTrace()) ? "No Stack Trace Error."
                : e.getStackTrace()[0].toString();
        return handleExceptionInternalFalse(e, ErrorStatus.INTERNAL_SERVER_ERROR,
                HttpHeaders.EMPTY, ErrorStatus.INTERNAL_SERVER_ERROR.getHttpStatus(), request,
                e.getMessage());
    }

    // 사용자 정의 예외 처리 (400번대)
    @ExceptionHandler(value = GlobalException.class)
    public ResponseEntity onThrowException(GlobalException globalException,
                                           HttpServletRequest request) {

        ErrorDetail errorDetail = globalException.getErrorDetail();
        return handleExceptionInternal(globalException, errorDetail, null, request);
    }

    private ResponseEntity<Object> handleExceptionInternal(Exception e, ErrorDetail detail,
                                                           HttpHeaders headers, HttpServletRequest request) {

        ApiResponse<Object> body = ApiResponse.onFailure(detail.code(), detail.message(),
                null);
        WebRequest webRequest = new ServletWebRequest(request);

        return super.handleExceptionInternal(
                e,
                body,
                headers,
                detail.httpStatus(),
                webRequest
        );
    }


    // 공통 예외 처리 메소드
    private ResponseEntity<Object> handleExceptionInternalFalse(Exception e,
                                                                ErrorStatus errorCommonStatus,
                                                                HttpHeaders headers, HttpStatus status, WebRequest request, String errorPoint) {
        ApiResponse<Object> body = ApiResponse.onFailure(errorCommonStatus.getCode(),
                errorCommonStatus.getMessage(), errorPoint);
        log.error(errorPoint);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                status,
                request
        );
    }

    // 서버 에러 처리 메소드
    private ResponseEntity<Object> handleExceptionInternalArgs(Exception e, HttpHeaders headers,
                                                               ErrorStatus errorCommonStatus,
                                                               WebRequest request, Map<String, String> errorArgs) {
        ApiResponse<Object> body = ApiResponse.onFailure(errorCommonStatus.getCode(),
                errorCommonStatus.getMessage(), errorArgs);
        log.error(errorArgs.toString());
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                errorCommonStatus.getHttpStatus(),
                request
        );
    }

    // 검증 실패에 대한 처리 메소드
    private ResponseEntity<Object> handleExceptionInternalConstraint(Exception e,
                                                                     HttpHeaders headers, WebRequest request, String message) {
        ApiResponse<Object> body = ApiResponse.onFailure(ErrorStatus.BAD_REQUEST.getCode(),
                message, null);
        log.error(message);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                ErrorStatus.BAD_REQUEST.getHttpStatus(),
                request
        );
    }
}