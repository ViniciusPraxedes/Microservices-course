package com.example.usersservice.service;

import com.example.usersservice.model.LoginDTO;
import org.junit.jupiter.api.Test;

import com.example.usersservice.model.RegisterDTO;
import com.example.usersservice.logoutToken.Token;
import com.example.usersservice.logoutToken.TokenRepository;
import com.example.usersservice.logoutToken.TokenType;
import com.example.usersservice.model.Role;
import com.example.usersservice.model.User;
import com.example.usersservice.repository.UserRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private AuthenticationService authService;
    @Test
    public void Register_RegisterUser_ReturnResponseEntityOKAndJWT() {
        //Given
        RegisterDTO registerDTO = new RegisterDTO();
        User testUser = new User();
        testUser.setFirstname("John");
        testUser.setLastname("Doe");
        testUser.setEmail("john@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.USER);

        //When
        when(userRepository.findByEmail(registerDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenReturn(testUser);
        when(jwtService.generateTokenWithoutExtraClaims(any())).thenReturn("generatedJwt");
        when(tokenRepository.save(any())).thenReturn(new Token("generatedJwt", TokenType.BEARER, false, false, testUser));
        ResponseEntity<?> responseEntity = authService.register(registerDTO);

        //Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("generatedJwt", responseEntity.getBody());
    }
    @Test
    public void Register_RegisterUser_EmailTaken() {
        //Given
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setEmail("john@example.com");
        User existingUser = new User();
        existingUser.setFirstname("John");
        existingUser.setLastname("Doe");
        existingUser.setEmail("john@example.com");
        existingUser.setPassword("encodedPassword");
        existingUser.setRole(Role.USER);

        //When
        when(userRepository.findByEmail(registerDTO.getEmail())).thenReturn(Optional.of(existingUser));

        //Then
        assertThrows(IllegalStateException.class, () -> {
            authService.register(registerDTO);
        });
    }

    @Test
    public void Login_LoginUser_ReturnResponseEntityOkAndJWT(){
        //Given
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("test");
        User testUser = new User();
        testUser.setFirstname("John");
        testUser.setLastname("Doe");
        testUser.setEmail("test@example.com");
        testUser.setPassword("test");
        testUser.setRole(Role.USER);

        //When
        when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(testUser));

        when(passwordEncoder.matches(loginDTO.getPassword(), testUser.getPassword())).thenReturn(true);

        when(jwtService.generateTokenWithoutExtraClaims(testUser)).thenReturn("generatedJwt");

        when(tokenRepository.save(any())).thenReturn(new Token("generatedJwt", TokenType.BEARER, false, false, testUser));

        when(authenticationManager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword()));

        //Then
        ResponseEntity<String> responseEntity = (ResponseEntity<String>) authService.login(loginDTO);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("generatedJwt", responseEntity.getBody());
    }
    @Test
    public void Login_LoginUser_UserNotFound(){
        //Given
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("test");

        //When
        when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.empty());

        //Then
        assertThrows(UsernameNotFoundException.class, () -> {
            authService.login(loginDTO);
        });

    }

    @Test
    public void Login_LoginUser_InvalidPassword() {
        //Given
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("test");
        User testUser = new User();
        testUser.setFirstname("John");
        testUser.setLastname("Doe");
        testUser.setEmail("test@example.com");
        testUser.setPassword("test");
        testUser.setRole(Role.USER);

        when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginDTO.getPassword(), testUser.getPassword())).thenReturn(false);

        assertThrows(UsernameNotFoundException.class, () -> {
            authService.login(loginDTO);
        });
    }
}
