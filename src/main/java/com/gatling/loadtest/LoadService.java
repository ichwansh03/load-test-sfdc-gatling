package com.gatling.loadtest;

import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class LoadService extends Simulation {

    public static final String ACCESS_TOKEN = "token";
    public static final String BASE_URL = "https://axaid-ccc--sbrlsdmtm.sandbox.my.salesforce.com";
    public static final String API_ENDPOINT = "/services/data/v61.0/sobjects/Case/";

    HttpProtocolBuilder builder = http
        .baseUrl(BASE_URL)
        .header("Authorization", "Bearer " + ACCESS_TOKEN)
        .header("Content-Type", "application/json")
            .header("Accept", "application/json");

    FeederBuilder<String> csvFeeder = csv("templates/LoadTestCase1.csv").circular();

    ScenarioBuilder insertCase = scenario("Insert Case")
            .feed(csvFeeder)
            .exec(
                    http("Insert Case Request")
                            .post(API_ENDPOINT)
                            .body(ElFileBody("templates/loaddata.json"))
                            .asJson()
                            .check(jsonPath("$.id").saveAs("caseId"))
            );

    {
        setUp(
                insertCase.injectOpen(rampUsers(7).during(5))
        ).protocols(builder);
    }
}
