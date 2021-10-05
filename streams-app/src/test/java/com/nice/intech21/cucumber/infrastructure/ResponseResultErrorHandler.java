package com.nice.intech21.cucumber.infrastructure;

import lombok.Getter;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

@Getter
class ResponseResultErrorHandler implements ResponseErrorHandler {
    private ResponseResults results = null;
    private boolean errored = false;

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        errored = response.getRawStatusCode() >= 400;
        return errored;
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        results = new ResponseResults(response);
    }
}
