package com.example.springcloud1;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
public class UriConfiguration {
    private String httpbin = "http://httpbin.org:80";
    private String loginbin = "http://localhost:8082";
    private String otherbin = "http://localhost:8081";


    public String getHttpbin() {
        return httpbin;
    }

    public void setHttpbin(String httpbin) {
        this.httpbin = httpbin;
    }

    public String getLoginbin() {
        return loginbin;
    }

    public void setLoginbin(String loginbin) {
        this.loginbin = loginbin;
    }

    public String getOtherBin() {
        return otherbin;
    }

    public void setOtherBin(String otherbin) {
        this.otherbin = otherbin;
    }
}
