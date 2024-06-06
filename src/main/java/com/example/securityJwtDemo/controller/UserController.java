package com.example.securityJwtDemo.controller;

import com.example.securityJwtDemo.dto.request.AddUserRequest;
import com.example.securityJwtDemo.dto.request.LoginRequest;
import com.example.securityJwtDemo.dto.request.RefreshTokenRequest;
import com.example.securityJwtDemo.dto.response.TokenResponse;
import com.example.securityJwtDemo.entity.RefreshToken;
import com.example.securityJwtDemo.entity.Users;
import com.example.securityJwtDemo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public TokenResponse login(@RequestBody LoginRequest loginRequest) {
        return userService.login(loginRequest);
    }

    @PostMapping("/addUser")
    @ResponseStatus(HttpStatus.CREATED)
    public void addUser(@RequestBody AddUserRequest request) throws Exception {
        userService.addUser(request);
    }

    @GetMapping("/admin/listUsers")
    @ResponseStatus(HttpStatus.OK)
    public List<Users> listUsers() {
        return userService.getAllUsers();
    }


    @PostMapping("/refreshtoken")
    public TokenResponse refreshtoken(@RequestBody RefreshTokenRequest token) {
        return userService.refreshtoken(token);
    }

    @PostMapping("/logout")
    public void logoutUser() {
        userService.logout();
    }

    @GetMapping("/admin/listRefresh")
    @ResponseStatus(HttpStatus.OK)
    public List<RefreshToken> listRefresh() {
        return userService.getAllRefreshToken();
    }
}
