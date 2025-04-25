package com.blooming.api.response.dto;

import com.blooming.api.entity.CartItemTypeEnum;
import lombok.Data;

@Data
public class CartItemDTO {
    private Long cartId;
    private String itemName;
    private CartItemTypeEnum itemType;
    private Integer price;
    private Integer quantity;
}
