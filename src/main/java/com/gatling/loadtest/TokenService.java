package com.gatling.loadtest;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;

@Repository
@RequiredArgsConstructor
public class TokenService {

    public static final String BASE_URL = "https://axaid-ccc--sbrlsdmtm.sandbox.my.salesforce.com";
    private final TokenRepository tokenRepository;

    public void saveToken(String token, long expiresIn) {
        Token sfdcToken = new Token();
        sfdcToken.setId("sfdc_token");
        sfdcToken.setAccessToken(token);

        sfdcToken.setCreatedAt(Instant.now());
        sfdcToken.setExpiresAt(Instant.now().plusSeconds(expiresIn));

        tokenRepository.save(sfdcToken);
    }

    public Token getTokenEntity() {
        return tokenRepository.findById("sfdc_token").orElse(null);
    }

    public String getToken() {
        return getTokenEntity() != null ? getTokenEntity().getAccessToken() : null;
    }

    public static String getTokenFromService() {
        try {

            String body =
                    "grant_type=client_credentials" +
                            "&client_id=3MVG9yj3UGaUlj7k3HX2usqyaeDhIC7galCRhSLs6NpDyJHE9dkUO0oToFC2DwZqsSb7JWl6m7URU4A.6f1W1" +
                            "&client_secret=82B840D7AF61B9E53C625BE1CA7A12736BE6206E3C8AD48B09307073C640AC4C";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/services/oauth2/token"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpClient httpClient = HttpClient.newHttpClient();
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to get token: " + response.body());
            }

            // Parse JSON response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(response.body());

            return jsonNode.get("access_token").asText();

        } catch (Exception e) {
            throw new RuntimeException("Failed to get access token", e);
        }
    }
}
