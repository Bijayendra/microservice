package com.bijayendra.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${gateway.security.public-paths:/actuator/**,/eureka/**,/swagger-ui/**,/v3/api-docs/**,/swagger-resources/**}")
    private String publicPaths;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        // Parse public paths from configuration
        List<String> publicPathList = Arrays.asList(publicPaths.split(","));
        String[] publicPathArray = publicPathList.stream()
            .map(String::trim)
            .toArray(String[]::new);

        http
            .csrf(csrf -> csrf.disable())
            .authorizeExchange(exchanges -> exchanges
                // Allow public access to configured endpoints
                .pathMatchers(publicPathArray).permitAll()
                // All other requests require authentication
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtDecoder(jwtDecoder()))
            );
        
        return http.build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        return ReactiveJwtDecoders.fromIssuerLocation(issuerUri);
    }
}
