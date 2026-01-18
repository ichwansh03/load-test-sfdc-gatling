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

public class LoadService extends Simulation {

    public static final String BASE_URL = "https://axaid-ccc--sbrlsdmtm.sandbox.my.salesforce.com";
    public static final String API_ENDPOINT = "/services/data/v61.0/sobjects/Call_Line__c/";
    public static final String ACCESS_TOKEN = getTokenFromService();

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
                insertCase.injectOpen(rampUsers(7).during(5))
        ).protocols(builder);
    }

    private static String getTokenFromService() {
        try {

            String body =
                    "grant_type=client_credentials" +
                            "&client_id=<id>" +
                            "&client_secret=<secret>";

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
