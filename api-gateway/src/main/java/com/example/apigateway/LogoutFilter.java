package com.example.apigateway;

import com.example.apigateway.logoutToken.TokenRepository;
import com.google.common.net.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class LogoutFilter extends AbstractGatewayFilterFactory<LogoutFilter.Config> {

    @Autowired
    JwtService jwtService;

    @Autowired
    TokenRepository tokenRepository;

    final Logger logger = LoggerFactory.getLogger(LogoutFilter.class);

    public LogoutFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Pre-processing
            System.out.println("Logout filter is being executed");

            //Get request
            ServerHttpRequest request = exchange.getRequest();

            //Extract auth header from request;
            String authorizationHeader = Objects.requireNonNull(request.getHeaders().get(HttpHeaders.AUTHORIZATION)).get(0);

            //Extract the jwt from the auth header
            String jwt = authorizationHeader.replace("Bearer ", "");

            var storedToken = tokenRepository.findByToken(jwt).orElse(null);
            if (storedToken != null && !storedToken.isRevoked() && !storedToken.isExpired()){
                storedToken.setExpired(true);
                storedToken.setRevoked(true);
                tokenRepository.save(storedToken);
            }else {
                throw new RuntimeException("Token is invalid");
            }

            return chain.filter(exchange);
        };
    }

    public static class Config {
        // ...
    }
}