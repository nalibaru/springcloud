
package com.example.springcloud1;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;

@Component
public class LoginPrefilter implements GatewayFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if ("/authenticate".equals(request.getURI().getPath()) && HttpMethod.POST.equals(request.getMethod())) {
            return DataBufferUtils.join(request.getBody())
                    .flatMap(dataBuffer -> {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer);
                        String body = new String(bytes, StandardCharsets.UTF_8);
                        String username = "Default User";
                        try {
                            User user = objectMapper.readValue(body, User.class);
                            username = user.getUsername() != null ? user.getUsername() : username;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        ServerHttpRequest modifiedRequest = exchange.getRequest()
                                .mutate()
                                .header("X-Username-filter", username) // Example modification
                                .build();
                        DataBuffer newDataBuffer = exchange.getResponse().bufferFactory().wrap(bytes);
                        Flux<DataBuffer> bodyFlux = Flux.just(newDataBuffer);
                        ServerHttpRequestDecorator decoratedRequest = new ServerHttpRequestDecorator(modifiedRequest) {
                            @Override
                            public Flux<DataBuffer> getBody() {
                                return bodyFlux;
                            }
                        };
                        ServerWebExchange modifiedExchange = exchange.mutate().request(decoratedRequest).build();
                        return chain.filter(modifiedExchange);
                    });
        }
        return chain.filter(exchange);
    }
}

