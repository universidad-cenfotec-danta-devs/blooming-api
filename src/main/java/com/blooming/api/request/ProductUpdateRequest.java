package com.blooming.api.request;

public record ProductUpdateRequest (String name,
                                    String description,
                                    double price
                                    ){
}
