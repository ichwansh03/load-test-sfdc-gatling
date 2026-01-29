package com.gatling.loadtest;

import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class LoadCallLineService extends Simulation {

    public static final String BASE_URL = "https://axaid-ccc--sbrlsdmtm.sandbox.my.salesforce.com";
    public static final String API_ENDPOINT = "/services/data/v61.0/sobjects/Call_Line__c/";
    public static final String ACCESS_TOKEN = TokenService.getTokenFromService();

    HttpProtocolBuilder builder = http
            .baseUrl(BASE_URL)
            .header("Authorization", "Bearer " + ACCESS_TOKEN)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json");

    FeederBuilder<String> csvFeeder = csv("templates/cr/LoadTestCR.csv").circular();

    ScenarioBuilder insertCase = scenario("Insert Call Line CR")
            .feed(csvFeeder)
            .exec(session -> {
                DateTimeFormatter formatter =
                        DateTimeFormatter.ofPattern("yyyy-MM-d'T'HH:mm:ss");
                String now =
                        LocalDateTime.now().format(formatter);
                return session.set("Collection_Call_Date__c", now);
            })
            .exec(
                    http("Insert Call Line CR - #{Id}")
                            .patch(API_ENDPOINT+"#{Id}")
                            .body(ElFileBody("templates/cr/loaddatacr.json"))
                            .asJson()
                            .check(status().is(204))
            );

    {
        setUp(
                insertCase.injectOpen(rampUsers(8).during(5))
        ).protocols(builder);
    }

}
