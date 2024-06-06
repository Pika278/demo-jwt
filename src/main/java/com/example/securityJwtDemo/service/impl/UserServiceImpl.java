package com.example.securityJwtDemo.service.impl;

import com.example.securityJwtDemo.dto.request.AddUserRequest;
import com.example.securityJwtDemo.dto.request.LoginRequest;
import com.example.securityJwtDemo.dto.request.RefreshTokenRequest;
import com.example.securityJwtDemo.dto.response.TokenResponse;
import com.example.securityJwtDemo.entity.RefreshToken;
import com.example.securityJwtDemo.entity.Users;
import com.example.securityJwtDemo.exception.CustomException;
import com.example.securityJwtDemo.repository.RefreshTokenRepository;
import com.example.securityJwtDemo.repository.UserRepository;
import com.example.securityJwtDemo.security.CustomUserDetails;
import com.example.securityJwtDemo.security.JwtGenerator;
import com.example.securityJwtDemo.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtGenerator jwtGenerator;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserServiceImpl(UserRepository userRepository, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, JwtGenerator jwtGenerator, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtGenerator = jwtGenerator;
        this.refreshTokenRepository = refreshTokenRepository;}


    @Override
    public TokenResponse login(LoginRequest loginRequest) {
        Users users = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Email not Found"));
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtGenerator.generatorToken(authentication);
        RefreshToken refreshToken = jwtGenerator.createRefreshToken(authentication);
        return new TokenResponse(token,refreshToken.getToken());
    }

    @Override
    public void addUser(AddUserRequest addUserRequest) throws Exception {
        if (userRepository.existsByEmail(addUserRequest.getEmail())) {
            throw new Exception("Email existed");
        }
        Users users = new Users(addUserRequest.getEmail(),passwordEncoder.encode(addUserRequest.getPassword()),addUserRequest.getRole());
        userRepository.save(users);
    }

    @Override
    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<RefreshToken> getAllRefreshToken() {
        return refreshTokenRepository.findAll();
    }

    @Override
    public void logout() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getUsers().getId();
        refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
    }

    @Override
    public TokenResponse refreshtoken(RefreshTokenRequest refreshTokenRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenRequest.getToken())
                .orElseThrow(() -> new CustomException("Refresh token is not in database!"));
        if(jwtGenerator.verifyRefreshTokenExpiration(refreshToken)) {
            String jwtToken = jwtGenerator.generatorToken(authentication);
            return new TokenResponse(jwtToken, refreshToken.getToken());
        }
        else {
            throw new CustomException("Refresh token expired!");
        }
    }
}
