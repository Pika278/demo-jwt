package com.example.securityJwtDemo.dto.response;

public class LoginResponse {
    private String accessToken;
    private String refreshToken;

    public LoginResponse() {
    }

    public LoginResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
