package com.qust.lab.exception;

import com.qust.lab.common.result.Result;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e
    ) {
        return Result.error(getFirstErrorMessage(e.getBindingResult()));
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        return Result.error(getFirstErrorMessage(e.getBindingResult()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(
            ConstraintViolationException e
    ) {
        String message = e.getConstraintViolations()
                .stream()
                .findFirst()
                .map(item -> item.getMessage())
                .orElse("请求参数不合法");

        return Result.error(message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e
    ) {
        return Result.error(
                "请求参数格式不正确，请检查 JSON 或枚举值"
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(
            IllegalArgumentException e
    ) {
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        return Result.error("服务器内部错误");
    }

    private String getFirstErrorMessage(BindingResult bindingResult) {
        return bindingResult.getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("请求参数不合法");
    }
}