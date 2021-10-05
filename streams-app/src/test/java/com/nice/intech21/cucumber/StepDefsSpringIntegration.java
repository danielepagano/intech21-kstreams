package com.nice.intech21.cucumber;

import com.nice.intech21.cucumber.infrastructure.CucumberTestInfrastructure;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StepDefsSpringIntegration extends CucumberTestInfrastructure {
    @When("the client calls actuator health check")
    public void theClientCallsActuatorHealthCheck() throws Throwable {
        executeGet("http://localhost:8080/actuator/health");
    }

    @Then("^the client receives status code of (\\d+)$")
    public void the_client_receives_status_code_of(int statusCode) throws Throwable {
        final HttpStatus currentStatusCode = latestResponse.getResponse().getStatusCode();
        assertEquals(statusCode, currentStatusCode.value(), "status code is incorrect : " + latestResponse.getBody());
    }

    @And("the client receives a healthy response")
    public void theClientReceivesAHealthyResponse() throws Throwable {
        assertEquals("UP", latestResponse.getBodyJson().getAsJsonObject().get("status").getAsString());
    }
}
