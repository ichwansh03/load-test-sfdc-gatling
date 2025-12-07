package com.gatling.loadtest;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final TokenService tokenService;
    private final RestTemplate restTemplate;

    @Value("${salesforce.client.auth2-token-url}")
    private String oauthUrl;

    @Value("${salesforce.client.id}")
    private String clientId;

    @Value("${salesforce.client.secret}")
    private String clientSecret;

    @Value("${salesforce.client.username}")
    private String username;

    @Value("${salesforce.client.password}")
    private String password;

    public String getAccessToken() {
        Token dbToken = tokenService.getTokenEntity();
        if (dbToken != null && Instant.now().isBefore(dbToken.getExpiresAt())) {
            return dbToken.getAccessToken();
        }

        return refreshToken();
    }

    private String refreshToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("username", username);
        form.add("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(oauthUrl, request, (Class<Map<String, Object>>)(Class<?>)Map.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to refresh token: " + response.getStatusCode());
        }

        Map<String, Object> responseBody = response.getBody();
        String accessToken = (String) responseBody.get("access_token");
        Integer expiresIn = (Integer) responseBody.get("expires_in");

        tokenService.saveToken(accessToken, expiresIn.longValue());

        return accessToken;
    }
}
