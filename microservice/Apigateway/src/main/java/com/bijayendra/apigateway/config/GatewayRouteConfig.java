package com.bijayendra.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Gateway routes.
 * You can define custom routes here or use service discovery.
 */
@Configuration
public class GatewayRouteConfig {

    /**
     * Example route configuration.
     * Routes can also be configured via application.properties/yml
     * or discovered automatically via Eureka.
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Example: Route for student service
            // .route("student-service", r -> r
            //     .path("/api/students/**")
            //     .uri("lb://student-service")
            //     .filters(f -> f
            //         .stripPrefix(1)
            //         .addRequestHeader("X-Gateway-Request", "true")
            //     )
            // )
            // Example: Route for address service
            // .route("address-service", r -> r
            //     .path("/api/addresses/**")
            //     .uri("lb://address-service")
            //     .filters(f -> f
            //         .stripPrefix(1)
            //         .addRequestHeader("X-Gateway-Request", "true")
            //     )
            // )
            .build();
    }
}
