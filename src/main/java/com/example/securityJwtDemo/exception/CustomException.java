package com.example.securityJwtDemo.exception;

public class CustomException extends RuntimeException{
    public CustomException(String message) {
        super(String.format("Failed for: %s", message));
    }
}
