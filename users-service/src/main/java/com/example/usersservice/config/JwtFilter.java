package com.example.usersservice.config;

import com.example.usersservice.service.JwtService;
import com.example.usersservice.logoutToken.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.security.Key;
import java.util.NoSuchElementException;
import java.util.function.Function;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private  JwtService jwtService;
    private  UserDetailsService userDetailsService;
    private  TokenRepository tokenRepository;
    private  HandlerExceptionResolver exceptionResolver;
    public JwtFilter(JwtService jwtService, UserDetailsService userDetailsService, TokenRepository tokenRepository, @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.tokenRepository = tokenRepository;
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;


        //Surrounds with try catch
        try{
        //If the header is null or does not have "Bearer " then jump to the next filter
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }



            jwt = authHeader.substring(7);
            userEmail = jwtService.extractUsername(jwt);

            //If email is not null and the Authentication was successful
            if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null){

                //Get user that was authenticated from database
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                //Find token and checks if it is valid
                var isTokenValid = tokenRepository.findByToken(jwt)
                        .map(t -> !t.isExpired() && !t.isRevoked())
                        .orElseThrow(() -> new RuntimeException("Something went wrong"));

                //If token is valid
                if(jwtService.isTokenValid(jwt,userDetails) && isTokenValid){

                    //Build authentication token with the user taken from the database
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    //Set the security context holder to the authentication token generated
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }

            }

        //Ends the try catch
        }catch (ExpiredJwtException | MalformedJwtException |NoSuchElementException e){
            exceptionResolver.resolveException(request, response, null, e);
        }


        //Jumps to the next filter
        filterChain.doFilter(request, response);

    }



}
