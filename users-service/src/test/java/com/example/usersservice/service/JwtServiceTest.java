package com.example.usersservice.service;

import com.example.usersservice.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.example.usersservice.service.JwtService.SECRET_KEY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
    @InjectMocks
    private JwtService jwtService;
    @Mock
    private Function<Claims, String> claimsResolverMock;
    @Mock
    private Function<Claims, Date> claimsExpirationResolver;



    //This method is needed to test extractClaim_ExtractClaimFromJwtToken_Success()
    private String generateSampleToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24))
                .signWith(getSingInKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    private String generateSampleTokenWithExpiration(Date expiration) {
        User userDetails = new User();
        return Jwts.builder()
                .setClaims(new HashMap<>())
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expiration)
                .signWith(getSingInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    //This method is needed to test extractClaim_ExtractClaimFromJwtToken_Success()
    private Key getSingInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }




    @Test
    void extractClaim_ExtractSpecificClaimFromJwtToken_ReturnClaim() {
        //Given
        User userDetails = new User();
        String token = generateSampleToken(new HashMap<>(), userDetails);

        //Makes claimsResolverMock return "claimValues"
        when(claimsResolverMock.apply( any() )).thenReturn("claimValue");

        //When
        String extractedClaim = jwtService.extractClaim(token, claimsResolverMock);


        //Then
        assertEquals("claimValue", extractedClaim);
    }
    @Test
    public void ExtractAllClaims_ExtractAllClaimsFromToken_ReturnALlClaims() {
        //Given
        User userDetails = new User();
        String token = generateSampleToken(new HashMap<>(), userDetails);

        //When
        Claims extractedClaims = jwtService.extractAllClaims(token);

        //Then
        assertNotNull(extractedClaims);
    }


    @Test
    void extractUsername_ExtractUsernameFromToken_ReturnUsername() {
        //Given
        User userDetails = new User();
        userDetails.setEmail("test@hotmail.com");
        String token = generateSampleToken(new HashMap<>(), userDetails);

        //When
        String username = jwtService.extractUsername(token);

        //Then
        Assertions.assertThat(username).isEqualTo("test@hotmail.com");
    }

    @Test
    void generateTokenWithExtraClaims_GenerateJwtTokenWithExtraClaims_ReturnToken() {
        //Given
        User userDetails = new User();
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("customClaim", "value");

        //When
        String generatedToken = jwtService.generateTokenWithExtraClaims(extraClaims, userDetails);

        //Then
        assertNotNull(generatedToken);


    }

    @Test
    public void testGenerateTokenWithoutExtraClaims_GenerateTokenWithoutExtraClaims_ReturnSimpleToken() {
        //Given
        User userDetails = new User();

        //When
        String generatedToken = jwtService.generateTokenWithoutExtraClaims(userDetails);

        //Then
        assertNotNull(generatedToken);
    }

    @Test
    void isTokenExpired_CheckIfTheTokenIsExpired_ReturnFalse() {
        //Given
        User userDetails = new User();
        String token = generateSampleToken(new HashMap<>(), userDetails);

        //When
        boolean isExpired = jwtService.isTokenExpired(token);

        //Then
        assertFalse(isExpired);
    }
    @Test
    void isTokenExpired_CheckIfTheTokenIsExpired_ReturnTrue() {
        //Given
        String token = generateSampleTokenWithExpiration(new Date(System.currentTimeMillis() - 1000));

        //When
        boolean isExpired = jwtService.isTokenExpired(token);

        //Then
        Assertions.assertThat(isExpired).isTrue();


    }


    @Test
    void isTokenValid() {
        //Given
        User userDetails = new User();
        userDetails.setEmail("test@hotmail.com");
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("customClaim", "value");

        //When
        String token = jwtService.generateTokenWithExtraClaims(extraClaims, userDetails);
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        //Then
        assertTrue(isValid);
    }
}