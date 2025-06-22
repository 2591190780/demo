package com.example.controller.exception;

import com.example.entity.RestBean;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class validationController {
    @ExceptionHandler(ConstraintViolationException.class)
    public RestBean<Void> validationException(ConstraintViolationException exception) {
        log.warn("Resolve [{}:{}]",exception.getClass().getName(),exception.getMessage());
        return RestBean.failure(400,"请求参数有误");
    }

    @ExceptionHandler(CustomerException.class)
    public RestBean<Void> validationException(CustomerException exception) {

        return RestBean.failure(500, exception.getMessage());
    }

}
