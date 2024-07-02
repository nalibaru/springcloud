package com.example.springcloud1;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class ModifyIdGatewayFilterFactory extends AbstractGatewayFilterFactory<ModifyIdGatewayFilterFactory.Config> {

    public ModifyIdGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String originalId = request.getQueryParams().getFirst("id");

            int modifiedId = Integer.parseInt(originalId);
            if (modifiedId < 10) {
                modifiedId = 5;
            } else {
                modifiedId = 1;
            }
            String finalModifiedId = String.valueOf(modifiedId);
            URI modifiedUri = UriComponentsBuilder.fromUri(request.getURI())
                    .replaceQueryParam("id", finalModifiedId)
                    .build(true)
                    .toUri();

            ServerHttpRequest modifiedRequest = request.mutate()
                    .uri(modifiedUri)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    public static class Config {
    }
}

