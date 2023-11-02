package com.example.usersservice.controller;


import com.example.usersservice.model.LoginDTO;
import com.example.usersservice.model.RegisterDTO;
import com.example.usersservice.logoutToken.TokenRepository;
import com.example.usersservice.service.AuthenticationService;
import com.example.usersservice.service.JwtService;
import com.example.usersservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;


import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;


@WebMvcTest(controllers = AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenRepository tokenRepository;
    @Mock
    private UserRepository userRepository;
    @MockBean
    private AuthenticationService authenticationService;
    /*
    @Test
    public void AuthenticationController_CreateUser_ReturnResponseEntity200AndJwt() throws Exception {
        //Given
        String jwt = "JwtToken";
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setEmail("test@hotmail.com");
        registerDTO.setFirstname("test");
        registerDTO.setLastname("test");
        registerDTO.setPassword("test");

        given(authenticationService.register(any())).willReturn(new ResponseEntity<>(jwt,HttpStatus.OK));

        //When
        ResultActions response = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)));


        //Then
        response
                .andExpect(status().isOk()).andDo(MockMvcResultHandlers.print())
                .andExpect(content().string(jwt));

    }
    @Test
    public void AuthenticationController_CreateUser_ReturnEmailTakenError() throws Exception {
        //Given
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setEmail("user@hotmail.com");
        registerDTO.setFirstname("test");
        registerDTO.setLastname("test");
        registerDTO.setPassword("test");


        given(authenticationService.register(any())).willReturn(new ResponseEntity<>("Email taken", HttpStatus.BAD_REQUEST));

        //When
        ResultActions response = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)));


        //Then
        response
                .andExpect(status().isBadRequest()).andDo(MockMvcResultHandlers.print())
                .andExpect(content().string("Email taken"));
    }

    @Test
    public void AuthenticationController_LoginValidCredentials_ReturnsJwt() throws Exception {
        // Given
        String jwt = "JwtToken";
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("test@hotmail.com");
        loginDTO.setPassword("password");

        when(authenticationService.login(any())).thenReturn(new ResponseEntity<>(jwt, HttpStatus.OK));

        // When
        ResultActions response = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)));

        // Then
        response
                .andExpect(status().isOk()) // Expect a 200 OK response
                .andExpect(content().string(jwt)); // Expect the JWT token in the response
    }

    @Test
    public void AuthenticationController_LoginInvalidCredentials_ReturnsUnauthorized() throws Exception {
        // Given
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("test@hotmail.com");
        loginDTO.setPassword("invalidpassword");

        when(authenticationService.login(any())).thenReturn(new ResponseEntity<>("Invalid credentials", HttpStatus.UNAUTHORIZED));

        // When
        ResultActions response = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)));

        // Then
        response
                .andExpect(status().isUnauthorized()) // Expect a 401 Unauthorized response
                .andExpect(content().string("Invalid credentials")); // Expect the error message in the response
    }







     */



}