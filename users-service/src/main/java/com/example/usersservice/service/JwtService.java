package com.example.usersservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    //Secret key used to sing the tokens
    public static final String SECRET_KEY = "Jjwpf3CxQefxQ5i2Gc6l6uD/NZAkFH+XmyK7VDb5lG6agLZpMQTV/E1vHYGRACfg";

    //Transform the secret key into bytes
    private Key getSingInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    //Extract all claims from token
    public Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSingInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    //Extract specific claim from token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    //Extract username from token
    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    //Generates new with extra claims token
    public String generateTokenWithExtraClaims(Map<String, Object> extraClaims, UserDetails userDetails){
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24))
                .signWith(getSingInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    //Generates new token without extra claims
    public String generateTokenWithoutExtraClaims(UserDetails userDetails){
        return generateTokenWithExtraClaims(new HashMap<>(), userDetails);
    }

    //Checks if token is expired
    public boolean isTokenExpired(String token){
        try{
            extractClaim(token, Claims::getExpiration).before(new Date());
        }catch (ExpiredJwtException e){
            return true;
        }
        return false;
    }

    //Checks if the token is valid
    public boolean isTokenValid(String token, UserDetails userDetails){
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }


}
