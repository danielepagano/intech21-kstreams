package com.nice.intech21.cucumber.infrastructure;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

@Getter
public class ResponseResults {
    private final ClientHttpResponse response;
    private final String body;

    public ResponseResults(final ClientHttpResponse response) throws IOException {
        this.response = response;
        final InputStream bodyInputStream = response.getBody();
        final StringWriter stringWriter = new StringWriter();
        IOUtils.copy(bodyInputStream, stringWriter, Charset.defaultCharset());
        this.body = stringWriter.toString();
    }

    public JsonElement getBodyJson() {
        return JsonParser.parseString(body);
    }
}
