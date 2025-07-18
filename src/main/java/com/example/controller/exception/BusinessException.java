package com.example.controller.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private int code; // 自定义错误码

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
    // Getter
}