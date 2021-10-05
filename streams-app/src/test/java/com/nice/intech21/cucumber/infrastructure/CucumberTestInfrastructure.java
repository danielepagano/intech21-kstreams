package com.nice.intech21.cucumber.infrastructure;

import com.nice.intech21.SampleStreamsApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@CucumberContextConfiguration
@SpringBootTest(classes = SampleStreamsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class CucumberTestInfrastructure {
    protected static ResponseResults latestResponse = null;

    @Test
    void contextLoads() {
    }

    @Autowired
    protected RestTemplate restTemplate;

    protected void executeGet(String url) {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        final HeaderSettingRequestCallback requestCallback = new HeaderSettingRequestCallback(headers);
        final ResponseResultErrorHandler errorHandler = new ResponseResultErrorHandler();

        restTemplate.setErrorHandler(errorHandler);
        latestResponse = restTemplate.execute(url, HttpMethod.GET, requestCallback, response -> {
            if (errorHandler.isErrored()) {
                return (errorHandler.getResults());
            } else {
                return (new ResponseResults(response));
            }
        });
    }
}
