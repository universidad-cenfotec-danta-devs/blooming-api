package com.blooming.api.request;

import lombok.Data;

@Data
public class PayPalRequest {
    double total;
    String description;
}
