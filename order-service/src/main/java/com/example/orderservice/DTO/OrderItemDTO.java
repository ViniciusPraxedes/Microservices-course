package com.example.orderservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDTO {
    private String itemCode;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
}
