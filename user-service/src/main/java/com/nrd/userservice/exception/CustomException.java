package com.nrd.userservice.exception;

public class CustomException extends RuntimeException {
    public CustomException(String message) {
        super(message);
    }
}