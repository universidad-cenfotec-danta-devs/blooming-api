package com.blooming.api.utils;

import com.blooming.api.response.http.GlobalHandlerResponse;
import com.blooming.api.response.http.MetaResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class PaginationUtils {

    public static ResponseEntity<?> getPaginatedResponse(Page<?> page, HttpServletRequest request) {
        if (page.isEmpty()) {
            return new GlobalHandlerResponse().handleResponse(HttpStatus.NO_CONTENT.name(),
                    page.getContent(), HttpStatus.OK, request);
        }

        MetaResponse metaResponse = new MetaResponse(
                request.getRequestURL().toString(),
                request.getMethod(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize()
        );
        return new GlobalHandlerResponse().handleResponse(HttpStatus.OK.name(), page.getContent(), HttpStatus.OK, metaResponse, request);
    }
}
