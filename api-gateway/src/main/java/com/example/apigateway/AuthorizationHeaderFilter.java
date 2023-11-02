package com.example.apigateway;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.example.apigateway.logoutToken.Token;
import com.example.apigateway.logoutToken.TokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ServerWebExchange;

import com.google.common.net.HttpHeaders;

import reactor.core.publisher.Mono;

@Component
@Transactional
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {
	
	@Autowired
	Environment env;

	@Autowired
	JwtService jwtService;
	@Autowired
	TokenRepository tokenRepository;
	
	public AuthorizationHeaderFilter() {
		super(Config.class);
	}

	public static class Config {
		// Put configuration properties here
	}

	@Override
	public GatewayFilter apply(Config config) {
		System.out.println("Auth header filter is being executed");
		return (exchange, chain) -> {

			//Get request
			ServerHttpRequest request = exchange.getRequest();

			//Extract auth header from request;
			String authorizationHeader = Objects.requireNonNull(request.getHeaders().get(HttpHeaders.AUTHORIZATION)).get(0);

			//Extract the jwt from the auth header
			String jwt = authorizationHeader.replace("Bearer ", "");

			//Extract the subject from the jwt
			String subject = jwtService.extractUsername(jwt);


			//Checks if token is expired or revoked
			if (tokenRepository.findByToken(jwt).isPresent() && tokenRepository.findByToken(jwt).get().isRevoked()){
				throw new RuntimeException("Jwt is revoked");
			}

			if (tokenRepository.findByToken(jwt).isEmpty() && !tokenRepository.findAll().isEmpty()){
				tokenRepository.revokeAndExpireAllTokensBySubject(subject);
			}



			//Extract revoked from the jwt
			boolean revoked = (boolean) jwtService.extractAllClaims(jwt).get("revoked");

			//Extract expired from the jwt
			boolean expired = jwtService.isTokenExpired(jwt);

			Token token = Token.builder()
					.token(jwt)
					.subject(subject)
					.revoked(revoked)
					.expired(expired)
					.build();


			System.out.println("are there more than one token not revoked: "+tokenRepository.hasMoreThanOneActiveToken(subject));

			if (tokenRepository.findByToken(jwt).isPresent() && !tokenRepository.findByToken(jwt).get().isRevoked()){

			}else {
				tokenRepository.save(token);

			}



			return chain.filter(exchange);
		};
	}
	
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);

        return response.setComplete();
    }





}
