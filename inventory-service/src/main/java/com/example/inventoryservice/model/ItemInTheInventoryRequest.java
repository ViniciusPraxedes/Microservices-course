package com.example.inventoryservice.model;

import lombok.Data;

@Data
public class ItemInTheInventoryRequest {
    private String itemCode;
    private Integer quantity;
}
