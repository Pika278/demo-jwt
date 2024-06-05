package com.example.securityJwtDemo.dto.request;

import com.example.securityJwtDemo.entity.Role;

public class AddUserRequest {
    private String email;
    private String password;
    private Role role;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }
}
