package com.argo.backend.auth.security.jwt;

import com.argo.backend.auth.exception.ExpiredTokenException;
import com.argo.backend.auth.exception.InvalidClaimTypeException;
import com.argo.backend.auth.exception.InvalidTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class JwtTokenProvider {
    private final Key key;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secretAccess}") String accessKey,
            @Value("${jwt.accessTokenExpirationMs}") long accessTokenExpirationMs,
            @Value("${jwt.refreshTokenExpirationMs}") long refreshTokenExpiration) {

        System.out.println("secretKey = '" + accessKey + "', length = " + accessKey.length());

        this.key = Keys.hmacShaKeyFor(accessKey.getBytes());    // 비밀키 주입
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpiration; // 만료시간 주입
    }

    public String generateAccessToken(Long userId, String username, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMs);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("username", username)
                .claim("role", roles)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Long userId, String username, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationMs);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("username", username)
                .claim("role", roles)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        String subject = claims.getSubject();

        return Long.parseLong(subject);
    }

    public String getUsernameFromToken(String token) {
        Claims claims = getClaims(token);

        Object usernameObj = claims.get("username");
        if (!(usernameObj instanceof String username)) {
            throw new InvalidClaimTypeException();
        }

        return username;
    }

    public List<String> getRolesFromToken(String token) {
        Claims claims = getClaims(token);

        Object roleClaim = claims.get("role");

        if (!(roleClaim instanceof List<?> roles)) {
            throw new InvalidClaimTypeException();
        }

        return roles.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public void validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
        } catch (SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            throw new InvalidTokenException();
        } catch (ExpiredJwtException e) {
            throw new ExpiredTokenException();
        }
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

}
