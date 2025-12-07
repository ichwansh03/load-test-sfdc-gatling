package com.gatling.loadtest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TokenController {

    private final RefreshTokenService tokenService;

    @GetMapping("/token")
    public String getToken() {
        return tokenService.getAccessToken();
    }
}
