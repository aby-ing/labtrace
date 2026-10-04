package com.qust.lab.exception;

import com.qust.lab.common.result.Result;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e
    ) {
        return error(
                HttpStatus.BAD_REQUEST,
                getFirstErrorMessage(e.getBindingResult())
        );
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBindException(
            BindException e
    ) {
        return error(
                HttpStatus.BAD_REQUEST,
                getFirstErrorMessage(e.getBindingResult())
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolation(
            ConstraintViolationException e
    ) {
        String message = e.getConstraintViolations()
                .stream()
                .findFirst()
                .map(item -> item.getMessage())
                .orElse("请求参数不合法");

        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e
    ) {
        return error(
                HttpStatus.BAD_REQUEST,
                "请求参数格式不正确，请检查 JSON 或枚举值"
        );
    }

    @ExceptionHandler({
            MissingRequestHeaderException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<Result<Void>> handleRequestParameterException(
            Exception e
    ) {
        return error(HttpStatus.BAD_REQUEST, "请求参数不正确");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException e
    ) {
        String method = e.getMethod() == null
                ? "当前"
                : e.getMethod();
        return error(
                HttpStatus.METHOD_NOT_ALLOWED,
                method + " 请求方法不被支持"
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException e
    ) {
        return error(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "请求内容类型不被支持"
        );
    }

    @ExceptionHandler({
            BadRequestException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<Result<Void>> handleBadRequest(
            RuntimeException e
    ) {
        return error(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Result<Void>> handleForbidden(
            ForbiddenException e
    ) {
        return error(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Result<Void>> handleUnauthorized(
            UnauthorizedException e
    ) {
        return error(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Result<Void>> handleNotFound(
            NotFoundException e
    ) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Result<Void>> handleConflict(
            ConflictException e
    ) {
        return error(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler({
            ServiceUnavailableException.class,
            IllegalStateException.class
    })
    public ResponseEntity<Result<Void>> handleServiceUnavailable(
            RuntimeException e
    ) {
        return error(
                HttpStatus.SERVICE_UNAVAILABLE,
                e.getMessage() == null
                        ? "当前服务暂时不可用"
                        : e.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        return error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "服务器内部错误"
        );
    }

    private ResponseEntity<Result<Void>> error(
            HttpStatus status,
            String message
    ) {
        String responseMessage = message == null || message.isBlank()
                ? status.getReasonPhrase()
                : message;

        return ResponseEntity
                .status(status)
                .body(Result.error(responseMessage));
    }

    private String getFirstErrorMessage(BindingResult bindingResult) {
        return bindingResult.getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("请求参数不合法");
    }
}
