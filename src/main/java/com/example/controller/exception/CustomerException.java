package com.example.controller.exception;


import lombok.Data;

@Data
public class CustomerException extends RuntimeException{


    private String  message;

    public CustomerException(String message){
        this.message = message;
    }
}
