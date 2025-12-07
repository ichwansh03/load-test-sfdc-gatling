package com.gatling.loadtest;

import lombok.Data;

@Data
public class Token {

    String endpoint;
    String grant_type;
    String client_id;
    String client_secret;
}
