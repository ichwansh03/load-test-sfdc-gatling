package com.gatling.loadtest;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
@RequiredArgsConstructor
public class TokenService {

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
}
