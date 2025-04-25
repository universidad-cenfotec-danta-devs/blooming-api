package com.blooming.api.response.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class PurchaseDTO {
    private Long id;
    private Long userId;
    private List<CartItemDTO> items;
    private Double totalAmount;
    private boolean active;
    private Date createdAt;
    private Date updatedAt;
}
