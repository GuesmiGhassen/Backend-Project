package com.example.bitbuckettesting.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class OAuthConfig {

    @Value("${bitbucket.client-id}")
    private String clientId;

    @Value("${bitbucket.client-secret}")
    private String clientSecret;

    @Value("${bitbucket.auth-url}")
    private String authUrl;

    @Value("${bitbucket.token-url}")
    private String tokenUrl;

    @Value("${bitbucket.auth-code}")
    private String authCode;

    private String accessToken;
    private final RestTemplate restTemplate = new RestTemplate();
    private long expiryTime;

    public String getAccessToken() {
//        if (accessToken == null || expiryTime < System.currentTimeMillis()) {
//            fetchAccessToken();
//        }
//        return accessToken;
        return "Sg2hUv_GpZiozT5-DW24bry-E3u1n6bZK0e_0dNepJN6-CpJNjAh3dkfKLhhw4d1wZseJ7Jglwz2RYfAkfjj4QBhbsa-iT-sR8ICDGuydFS0v1j7R0CUU5VVpGs6oau9vcoltUnVAHwvDqzVXUS7n5C1kCI=";
    }

    private void fetchAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        String body = "grant_type=authorization_code&code=" + authCode;
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            accessToken = (String) response.getBody().get("access_token");
            expiryTime = System.currentTimeMillis() + ((Integer) response.getBody().get("expires_in")) * 4000;
        }
    }

}
