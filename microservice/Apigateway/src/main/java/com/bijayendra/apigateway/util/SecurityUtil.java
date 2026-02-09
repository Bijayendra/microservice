package com.bijayendra.apigateway.util;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Utility class for extracting security information from JWT tokens.
 */
public class SecurityUtil {

    /**
     * Get the current authenticated user's ID from JWT token.
     */
    public static Mono<String> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
            .cast(SecurityContext.class)
            .map(SecurityContext::getAuthentication)
            .filter(auth -> auth != null && auth.getPrincipal() instanceof Jwt)
            .cast(Jwt.class)
            .map(jwt -> jwt.getClaimAsString("sub"))
            .cast(String.class);
    }

    /**
     * Get the current authenticated user's username from JWT token.
     */
    public static Mono<String> getCurrentUsername() {
        return ReactiveSecurityContextHolder.getContext()
            .cast(SecurityContext.class)
            .map(SecurityContext::getAuthentication)
            .filter(auth -> auth != null && auth.getPrincipal() instanceof Jwt)
            .cast(Jwt.class)
            .map(jwt -> {
                String username = jwt.getClaimAsString("preferred_username");
                return username != null ? username : jwt.getClaimAsString("username");
            })
            .cast(String.class);
    }

    /**
     * Get the current authenticated user's roles/authorities from JWT token.
     */
    public static Mono<List<String>> getCurrentUserRoles() {
        return ReactiveSecurityContextHolder.getContext()
            .cast(SecurityContext.class)
            .map(SecurityContext::getAuthentication)
            .filter(auth -> auth != null && auth.getPrincipal() instanceof Jwt)
            .cast(Jwt.class)
            .map(jwt -> {
                List<String> roles = jwt.getClaimAsStringList("authorities");
                if (roles == null) {
                    roles = jwt.getClaimAsStringList("roles");
                }
                return roles != null ? roles : List.of();
            })
            .cast(List.class);
    }

    /**
     * Check if the current user has a specific role.
     */
    public static Mono<Boolean> hasRole(String role) {
        return getCurrentUserRoles()
            .map(roles -> roles.contains(role) || roles.contains("ROLE_" + role));
    }

    /**
     * Get the full JWT token for the current user.
     */
    public static Mono<Jwt> getCurrentJwt() {
        return ReactiveSecurityContextHolder.getContext()
            .cast(SecurityContext.class)
            .map(SecurityContext::getAuthentication)
            .filter(auth -> auth != null && auth.getPrincipal() instanceof Jwt)
            .cast(Jwt.class);
    }
}
