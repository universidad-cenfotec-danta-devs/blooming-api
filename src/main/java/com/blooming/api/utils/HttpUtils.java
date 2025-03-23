package com.blooming.api.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class HttpUtils {

    public static HttpHeaders createHeaders(String apiKey) {
        return new HttpHeaders() {{
            setBearerAuth(apiKey);
            setContentType(MediaType.APPLICATION_JSON);
        }};
    }
}
