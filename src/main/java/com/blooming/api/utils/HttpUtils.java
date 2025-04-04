package com.blooming.api.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class HttpUtils {

    public static HttpHeaders createHeadersForOpenAI(String apiKey) {
        return new HttpHeaders() {{
            setBearerAuth(apiKey);
            setContentType(MediaType.APPLICATION_JSON);
        }};
    }

    public static HttpHeaders createHeadersForPlantId(String apiKey) {
        return new HttpHeaders() {{
            set("Api-Key", apiKey);
            setContentType(MediaType.APPLICATION_JSON);
        }};
    }
}
