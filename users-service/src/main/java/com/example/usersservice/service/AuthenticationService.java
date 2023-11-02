package com.example.usersservice.service;

import com.example.usersservice.model.*;
import com.example.usersservice.logoutToken.Token;
import com.example.usersservice.logoutToken.TokenRepository;
import com.example.usersservice.logoutToken.TokenType;
import com.example.usersservice.repository.UserRepository;
import jakarta.ws.rs.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthenticationService {
    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;
    private TokenRepository tokenRepository;
    private JwtService jwtService;
    private AuthenticationManager authenticationManager;

    public AuthenticationService(PasswordEncoder passwordEncoder, UserRepository userRepository, TokenRepository tokenRepository, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public ResponseEntity<?> register(RegisterDTO body){

        //Checks if user with the same email already exists in the database
        if (userRepository.findByEmail(body.getEmail()).isPresent()){
            throw new IllegalStateException("Email taken");
        }else {

            //Creates user
            User user = new User(
                    body.getFirstname(),
                    body.getLastname(),
                    body.getEmail(),
                    passwordEncoder.encode(body.getPassword()),
                    body.getAddress(),
                    body.getCity(),
                    body.getPostcode(),
                    body.getPhoneNumber(),
                    Role.USER);

            //Adds user to the database
            userRepository.save(user);

            //Returns User and http status 200
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
    }

    public ResponseEntity<?> login(LoginDTO body){

        //Checks if user exists in the database and if the password matches
        if (userRepository.findByEmail(body.getEmail()).isPresent() && passwordEncoder.matches(body.getPassword(), userRepository.findByEmail(body.getEmail()).get().getPassword())){

            //Then generate an authentication token with this user credentials
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(body.getEmail(),body.getPassword());

            //Authenticate user
            authenticationManager.authenticate(authenticationToken);

            //Generate jwt token
            //String jwt = jwtService.generateTokenWithoutExtraClaims(userRepository.findByEmail(body.getEmail()).get());

            Map<String, Object> extraClaims = new HashMap<>();
            boolean revoked = false;

            extraClaims.put("authority", userRepository.findByEmail(body.getEmail()).get().getAuthorities().toString());
            extraClaims.put("revoked", revoked);

            String jwt = jwtService.generateTokenWithExtraClaims(extraClaims,userRepository.findByEmail(body.getEmail()).get());

            //Generates this token in order to save the jwt token to the database
            Token token = new Token(jwt, TokenType.BEARER,false,false,userRepository.findByEmail(body.getEmail()).get());

            //Revoke all existing tokens before saving the new one to the database
            revokeAllUserTokens(userRepository.findByEmail(body.getEmail()).get());

            //Saves the token to the database
            tokenRepository.save(token);

            //Return jwt token and status code 200
            return new ResponseEntity<>(jwt,HttpStatus.OK);

        }else {
            throw new UsernameNotFoundException("User not found");
        }

    }

    private void revokeAllUserTokens(User user){
        //Gets all tokens associated with the user
        var validUserTokens = tokenRepository.findAllValidTokensByUserId(user.getId());

        //Checks if there are valid tokens
        if (validUserTokens.isEmpty()) {
        }else {

            //Set expire and revoked to true for all tokens
            validUserTokens.forEach(t -> {
                t.setExpired(true);
                t.setRevoked(true);
            });

            //Save them to the database
            tokenRepository.saveAll(validUserTokens);
        }
    }

}
