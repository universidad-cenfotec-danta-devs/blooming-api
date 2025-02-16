package com.blooming.api.response.http;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetaResponse {

    private String method;
    private String url;
    private int totalPages;
    private long totalElements;
    private int pageNumber;
    private int pageSize;

    public MetaResponse(String method, String url) {
        this.method = method;
        this.url = url;
    }

    public MetaResponse(String url, String method, int totalPages, long totalElements, int pageNumber, int pageSize) {
        this.url = url;
        this.method = method;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }
}