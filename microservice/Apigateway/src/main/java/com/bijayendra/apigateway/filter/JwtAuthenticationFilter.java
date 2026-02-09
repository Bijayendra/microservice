package com.bijayendra.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Global filter to extract JWT token information and add it to request headers
 * for downstream services. This filter runs after authentication.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USERNAME_HEADER = "X-Username";
    private static final String AUTHORITIES_HEADER = "X-Authorities";
    private static final String EMAIL_HEADER = "X-User-Email";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
            .cast(SecurityContext.class)
            .map(SecurityContext::getAuthentication)
            .filter(authentication -> authentication != null && authentication.getPrincipal() instanceof Jwt)
            .cast(Jwt.class)
            .flatMap(jwt -> {
                // Extract user information from JWT token
                String userId = jwt.getClaimAsString("sub"); // Subject (user ID)
                String username = jwt.getClaimAsString("preferred_username");
                if (username == null) {
                    username = jwt.getClaimAsString("username");
                }
                String email = jwt.getClaimAsString("email");
                
                // Extract authorities/roles
                List<String> authorities = jwt.getClaimAsStringList("authorities");
                if (authorities == null) {
                    authorities = jwt.getClaimAsStringList("roles");
                }
                
                // Add user information to request headers for downstream services
                ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(request -> {
                        if (userId != null) {
                            request.header(USER_ID_HEADER, userId);
                        }
                        if (username != null) {
                            request.header(USERNAME_HEADER, username);
                        }
                        if (email != null) {
                            request.header(EMAIL_HEADER, email);
                        }
                        if (authorities != null && !authorities.isEmpty()) {
                            request.header(AUTHORITIES_HEADER, String.join(",", authorities));
                        }
                    })
                    .build();
                
                logger.debug("Added user headers - UserId: {}, Username: {}, Email: {}", 
                    userId, username, email);
                
                return chain.filter(modifiedExchange);
            })
            .switchIfEmpty(chain.filter(exchange)); // If no JWT, continue without modification
    }

    @Override
    public int getOrder() {
        // Run after authentication but before other filters
        return -100;
    }
}
