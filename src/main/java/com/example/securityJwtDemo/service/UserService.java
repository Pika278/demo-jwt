package com.example.securityJwtDemo.service;

import com.example.securityJwtDemo.dto.request.AddUserRequest;
import com.example.securityJwtDemo.dto.request.LoginRequest;
import com.example.securityJwtDemo.dto.request.RefreshTokenRequest;
import com.example.securityJwtDemo.dto.response.TokenResponse;
import com.example.securityJwtDemo.entity.RefreshToken;
import com.example.securityJwtDemo.entity.Users;

import java.util.List;

public interface UserService {
    TokenResponse login(LoginRequest loginRequest);
    void addUser(AddUserRequest addUserRequest) throws Exception;
    List<Users> getAllUsers();
    List<RefreshToken> getAllRefreshToken();
    void logout();
    TokenResponse refreshtoken(RefreshTokenRequest refreshTokenRequest);
}
