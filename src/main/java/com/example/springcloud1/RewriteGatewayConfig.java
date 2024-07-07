package com.example.springcloud1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.addOriginalRequestUrl;


@Configuration
public class RewriteGatewayConfig {
    private static final Logger logger = LoggerFactory.getLogger(GatewayConfig.class);
    @Bean
    public RouteLocator myRoutesForRewriteParams(RouteLocatorBuilder builder, UriConfiguration uriConfiguration, ModifyIdGatewayFilterFactory modifyIdFilter) {
        String loginURl = uriConfiguration.getLoginbin();
        return builder.routes()
                //Rewrite the path before sending request to downstream
                .route("route15", r -> r.path("/api/redirect")
                        .filters(f -> f.filter((exchange, chain) -> {
                            ServerHttpRequest req = exchange.getRequest();
                            addOriginalRequestUrl(exchange, req.getURI());
                            String path = req.getURI().getRawPath();
                            String newPath = path.replaceAll(
                                    "/api/redirect",
                                    "/v1/request");
                            ServerHttpRequest request = req.mutate().path(newPath).build();
                            //exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, request.getURI());
                            return chain.filter(exchange.mutate().request(request).build());
                        }))
                        .uri(loginURl))
                //rewrite the response header from the downstream
                .route("route16", r -> r.path("/api/rewriteres")
                        .filters(f -> f.addRequestHeader("Hello", "World")
                                .rewriteResponseHeader("X-Request-Red", "(password=[^&]+)", "password=***"))
                        .uri(loginURl))
                //rewrite the request parameter from the downstream
                .route("route17", r -> r.path("/api/rewritereqid")
                        .filters(f -> f.rewriteRequestParameter("id", "5"))
                        .uri(loginURl))
                .build();
    }
}
