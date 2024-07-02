package com.example.springcloud1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Map;


@Configuration
public class GatewayConfig {
    private static final Logger logger = LoggerFactory.getLogger(GatewayConfig.class);
    @Bean
    public RouteLocator myRoutesForEditParams(RouteLocatorBuilder builder, UriConfiguration uriConfiguration, ModifyIdGatewayFilterFactory modifyIdFilter,LoginPrefilter loginPrefilter) {
        String loginURl = uriConfiguration.getLoginbin();
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
                .route("route5", r -> r.path("/api/editrequest")
                        .filters(f -> f.addRequestHeader("Hello", "World")
                                .modifyRequestBody(Map.class, User.class, (exchange, map) -> {
                                    String username = map.containsKey("username") ? (String) map.get("username") : "";
                                    Long id= Long.valueOf("100");
                                    String password = "******";
                                    String address = "JPN";
                                    Integer age = map.containsKey("age") ? (Integer) map.get("age") : 18;
                                    User user = new User( username.toUpperCase(), password, address,age);
                                    return Mono.just(user);
                                }))
                        .uri(loginURl))

                .route("route6", r -> r.path("/api/editresponse")
                        .filters(f -> f
                                .addRequestHeader("Hello", "World")
                                .modifyResponseBody(Map.class, User.class, (exchange, map) -> {
                                    String username = map.containsKey("username") ? (String) map.get("username") : "";
                                    String password = map.containsKey("password") ? (String) map.get("password") : null;
                                    String address = "USA";
                                    Integer age = 18;
                                    User user = new User( username.toUpperCase(), password, address,age);
                                    return Mono.just(user);
                                }))
                        .uri(loginURl))
                .route("route7", r -> r.path("/api/adduser")
                        .filters(f -> f.addRequestHeader("Hello", "World")
                                .modifyRequestBody(Map.class, User.class, (exchange, map) -> {
                                    String username = map.containsKey("username") ? (String) map.get("username") : "";
                                    String password = map.containsKey("password") ? (String) map.get("password") : "";
                                    String address = "JPN";
                                    Integer age = map.containsKey("age") ? (Integer) map.get("age") : 18;
                                    User user = new User( username.toUpperCase(), password, address,age);
                                    return Mono.just(user);
                                }))
                        .uri(loginURl))
                .route("route8", r -> r.path("/api/editreqid")
                        .filters(f -> f.addRequestHeader("Hello", "World")
                                .filter(modifyIdFilter.apply(new ModifyIdGatewayFilterFactory.Config())))
                        .uri(loginURl))
                .build();
    }

}
