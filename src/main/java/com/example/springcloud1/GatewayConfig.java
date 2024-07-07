package com.example.springcloud1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;
import java.util.Map;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.addOriginalRequestUrl;


@Configuration
public class GatewayConfig {
    private static final Logger logger = LoggerFactory.getLogger(GatewayConfig.class);
    @Bean
    public RouteLocator myRoutesForEditParams(RouteLocatorBuilder builder, UriConfiguration uriConfiguration, ModifyIdGatewayFilterFactory modifyIdFilter,LoginPrefilter loginPrefilter) {
        String loginURl = uriConfiguration.getLoginbin();
        return builder.routes()
                //Loginfilter adding request header after fetching username from the reqbody
                .route("route1", r -> r.path("/authenticate")
                        .filters(f -> f.filter(loginPrefilter))
                        .uri(loginURl))
                //adding request header and response header in downstream
                .route("route2", r -> r.path("/api/request")
                        .filters(f -> f.addRequestHeader("Hello", "World"))
                        .uri(loginURl))
                //adding response header
                .route("route3", r -> r.path("/api/fetchuser")
                        .filters(f -> f.addResponseHeader("Hello", "World"))
                        .uri(loginURl))
                //map request header - will get the header from downstream for Hello and add a response header with to header and request header value
                .route("route4", r -> r.path("/api/reqheader")
                        .filters(f -> f.mapRequestHeader("Hello", "X-profilename"))
                        .uri(loginURl))
                //override the header from downstream
                .route("route10", r -> r.path("/api/setreqheader")
                        .filters(f -> f.setRequestHeader("Hello", "World"))
                        .uri(loginURl))
                //override the response header from downstream
                .route("route11", r -> r.path("/api/setresheader")
                        .filters(f -> f.setResponseHeader("X-Custom-Header", "New Version"))
                        .uri(loginURl))
                //remove the header from downstream
                .route("route11", r -> r.path("/api/remreqheader")
                        .filters(f -> f.removeRequestHeader("Hello"))
                        .uri(loginURl))
                //remove the response header from downstream
                .route("route12", r -> r.path("/api/remresheader")
                        .filters(f -> f.removeResponseHeader("X-Custom-Header"))
                        .uri(loginURl))
                //remove request query parameter from downstream URL
                .route("route13", r -> r.path("/api/removereqid")
                        .filters(f -> f.removeRequestParameter("id"))
                        .uri(loginURl))
                //add request header and modify request body before sending to downstream
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
                //add request header, modify response body before sending to client
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
                //add reqheader, modify requestbody before sending to downstream
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
                //edit the req query parameter before sending to downstream
                .route("route8", r -> r.path("/api/editreqid")
                        .filters(f -> f.addRequestHeader("Hello", "World")
                                .filter(modifyIdFilter.apply(new ModifyIdGatewayFilterFactory.Config())))
                        .uri(loginURl))
                //add parameter before sending to downstream
                .route("route9", r -> r.path("/api/fetchreqid")
                        .filters(f -> f.addRequestParameter("id","5"))
                        .uri(loginURl))
                //redirect the request url to another
                .route("route14", r -> r.path("/api/redirect")
                        .filters(f -> f.filter((exchange, chain) -> {
                            ServerHttpRequest req = exchange.getRequest();
                            addOriginalRequestUrl(exchange, req.getURI());
                            String path = req.getURI().getRawPath();
                            String newPath = path.replaceAll(
                                    "/api/redirect",
                                    "/v1/request");
                            ServerHttpRequest request = req.mutate().path(newPath).build();
                            return chain.filter(exchange.mutate().request(request).build());
                        }))
                        .uri(loginURl))
                .build();

    }

}
