package com.example.securityJwtDemo;

public class CustomException extends RuntimeException{
    public CustomException(String message) {
        super(String.format("Failed for: %s", message));
    }
}
