package com.gatling.loadtest;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "sfdc_token")
public class Token {

    @Id
    private String id;
    private String accessToken;
    private Instant createdAt;
    private Instant expiresAt;
}
