package com.example.springcloud1.configuration;


import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class RouterConfiguration {

    //Prefilter for all services before sending to downstream
    @Bean
    public GlobalFilter customGlobalFilter() {
        return (exchange, chain) -> {
            Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            String routeId = route != null ? route.getId() : "no-route-found";
            ServerHttpRequest modifiedRequest = exchange.getRequest()
                    .mutate()
                    .header("project-name", "SpringCloud")
                    .build();
            ServerWebExchange modifiedExchange = exchange.mutate().request(modifiedRequest).build();
            return chain.filter(modifiedExchange);
        };
    }

    //Postfilter for all services before sending to client
    @Bean
    public GlobalFilter customGlobalPostFilter() {
        return (exchange, chain) -> chain.filter(exchange)
                .then(Mono.just(exchange))
                .map(serverWebExchange -> {
                    serverWebExchange.getResponse().getHeaders().set("CUSTOM-RESPONSE-HEADER",
                            HttpStatus.OK.equals(serverWebExchange.getResponse().getStatusCode()) ? "It worked" : "It did not work");
                    serverWebExchange.getResponse().getHeaders().set("project-name", "SpringCloud");

                    return serverWebExchange;
                })
                .then();
    }


    @Bean
    public GlobalFilter customProjectNameEditFilter() {
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
