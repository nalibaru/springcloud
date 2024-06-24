package com.example.springcloud1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.cloud.gateway.route.Route;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@EnableConfigurationProperties(UriConfiguration.class)
public class SpringCloud1Application {
    public static void main(String[] args) {
        SpringApplication.run(SpringCloud1Application.class, args);
    }

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder,UriConfiguration uriConfiguration,LoginPrefilter loginPrefilter) {
        String loginURl = uriConfiguration.getLoginbin();
        String otherURL = uriConfiguration.getOtherBin();
        return builder.routes()
                .route("route1", r -> r.path("/authenticate")
                        .filters(f -> f.filter(loginPrefilter))
                        .uri(loginURl))
                .route("route2", r -> r.path("/api/request")
                        .filters(f -> f.addRequestHeader("Hello", "World"))
                        .uri(loginURl))
                .route("route3", r -> r.path("/api/fetchuser")
                        .filters(f -> f.addRequestHeader("Hello", "World"))
                        .uri(loginURl))
                .route("route4", r -> r.path("/api/reqheader")
                        .filters(f -> f.addRequestHeader("Hello", "World"))
                        .uri(loginURl))
                .build();
    }

    @Bean
    public GlobalFilter customGlobalFilter() {
        return (exchange, chain) -> {
            Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            String routeId = route != null ? route.getId() : "no-route-found";
            ServerHttpRequest modifiedRequest = exchange.getRequest()
                    .mutate()
                    .header("custom-api-header", routeId) // Set route ID as header
                    .build();
            ServerWebExchange modifiedExchange = exchange.mutate().request(modifiedRequest).build();
            return chain.filter(modifiedExchange);
        };
    }

    @Bean
    public GlobalFilter customGlobalPostFilter() {
        return (exchange, chain) -> chain.filter(exchange)
                .then(Mono.just(exchange))
                .map(serverWebExchange -> {
                    serverWebExchange.getResponse().getHeaders().set("CUSTOM-RESPONSE-HEADER",
                            HttpStatus.OK.equals(serverWebExchange.getResponse().getStatusCode()) ? "It worked" : "It did not work");
                    return serverWebExchange;
                })
                .then();
    }

    @Bean
    public GlobalFilter loggingFilter() {
        return (exchange, chain) -> {
            // Log the incoming request
            System.out.println("Incoming request: " + exchange.getRequest().getPath());
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                // Log the outgoing response
                System.out.println("Outgoing response: " + exchange.getResponse().getStatusCode());
            }));
        };
    }
}

