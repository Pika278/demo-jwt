package com.example.securityJwtDemo.configuration;

import com.example.securityJwtDemo.CustomException;
import com.example.securityJwtDemo.entity.RefreshToken;
import com.example.securityJwtDemo.repository.RefreshTokenRepository;
import com.example.securityJwtDemo.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.UUID;


import java.security.Key;
import java.util.Date;

@Component
public class JwtGenerator {
    private static final Key key =  Keys.secretKeyFor(SignatureAlgorithm.HS512);
    public static final long JWT_EXPIRATION = 60000;
    public static final long JWT_REFRESH_EXPIRATION = 120000;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtGenerator(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String generatorToken(Authentication authentication) {
        String username = authentication.getName();
        Date currentDate = new Date();
        Date expiredDate = new Date(currentDate.getTime() + JWT_EXPIRATION);

        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expiredDate)
                .signWith(key,SignatureAlgorithm.HS512)
                .compact();
        return token;
    }

    public String getUsernameFromJWT(String token) {
         Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                 .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        }catch (Exception e) {
            throw new AuthenticationCredentialsNotFoundException("JWT was expired or incorrect");
        }

    }

    public RefreshToken createRefreshToken(Authentication authentication) {
        RefreshToken refreshToken = new RefreshToken();
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = customUserDetails.getUsers().getId();
        refreshToken.setUser(userRepository.findById(userId).get());
        refreshToken.setExpiryDate(Instant.now().plusMillis(JWT_REFRESH_EXPIRATION));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public boolean verifyRefreshTokenExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new CustomException("Refresh token was expired. Please make a new signin request");
        }
        return true;
    }
}
