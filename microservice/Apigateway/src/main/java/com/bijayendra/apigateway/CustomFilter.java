package com.bijayendra.apigateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Custom global filter for logging request/response information.
 * Enhanced to work with OAuth2 authentication.
 */
@Component
public class CustomFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(CustomFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Log request information
        logger.info("Request URI: {}", request.getURI());
        logger.info("Request Method: {}", request.getMethod());
        
        // Log Authorization header (if present)
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null) {
            logger.info("Authorization Header: {}", 
                authHeader.length() > 20 ? authHeader.substring(0, 20) + "..." : authHeader);
        }
        
        // Log authenticated user information if available
        return ReactiveSecurityContextHolder.getContext()
            .cast(SecurityContext.class)
            .map(SecurityContext::getAuthentication)
            .filter(auth -> auth != null && auth.getPrincipal() instanceof Jwt)
            .cast(Jwt.class)
            .doOnNext(jwt -> {
                String userId = jwt.getClaimAsString("sub");
                String username = jwt.getClaimAsString("preferred_username");
                logger.info("Authenticated User - ID: {}, Username: {}", userId, username);
            })
            .then(chain.filter(exchange))
            .then(Mono.fromRunnable(() -> {
                ServerHttpResponse response = exchange.getResponse();
                logger.info("Response Status: {}", response.getStatusCode());
            }));
    }

    @Override
    public int getOrder() {
        // Run after JwtAuthenticationFilter
        return -50;
    }
}
