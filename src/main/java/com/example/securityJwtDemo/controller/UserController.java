package com.example.securityJwtDemo.controller;

import com.example.securityJwtDemo.CustomException;
import com.example.securityJwtDemo.configuration.CustomUserDetails;
import com.example.securityJwtDemo.configuration.JwtGenerator;
import com.example.securityJwtDemo.dto.request.AddUserRequest;
import com.example.securityJwtDemo.dto.request.LoginRequest;
import com.example.securityJwtDemo.dto.request.RefreshTokenRequest;
import com.example.securityJwtDemo.dto.response.LoginResponse;
import com.example.securityJwtDemo.entity.RefreshToken;
import com.example.securityJwtDemo.entity.Users;
import com.example.securityJwtDemo.repository.RefreshTokenRepository;
import com.example.securityJwtDemo.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/")
public class UserController {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtGenerator jwtGenerator;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserController(UserRepository userRepository, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, JwtGenerator jwtGenerator, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtGenerator = jwtGenerator;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        Users users = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Email not Found"));
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtGenerator.generatorToken(authentication);
        RefreshToken refreshToken = jwtGenerator.createRefreshToken(authentication);
        return new LoginResponse(token,refreshToken.getToken());
    }

    @PostMapping("/addUser")
    @ResponseStatus(HttpStatus.CREATED)
    public void addUser(@RequestBody AddUserRequest request) throws Exception {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new Exception("Email existed");
        }
        Users users = new Users(request.getEmail(),passwordEncoder.encode(request.getPassword()),request.getRole());
        userRepository.save(users);
    }

    @GetMapping("/admin/listUsers")
    @ResponseStatus(HttpStatus.OK)
    public List<Users> listUsers(){
        return userRepository.findAll();
    }


    @PostMapping("/refreshtoken")
    public LoginResponse refreshtoken(@RequestBody RefreshTokenRequest token) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token.getToken())
                .orElseThrow(() -> new CustomException("Refresh token is not in database!"));
        if(jwtGenerator.verifyRefreshTokenExpiration(refreshToken)) {
            String jwtToken = jwtGenerator.generatorToken(authentication);
            return new LoginResponse(jwtToken, refreshToken.getToken());
        }
        else {
            throw new CustomException("Refresh token expired!");
        }
    }

    @PostMapping("/logout")
    public String logoutUser() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getUsers().getId();
        refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
        return "Log out successful!";
    }

    @GetMapping("/admin/listRefresh")
    @ResponseStatus(HttpStatus.OK)
    public List<RefreshToken> listRefresh(){
        return refreshTokenRepository.findAll();
    }
}
