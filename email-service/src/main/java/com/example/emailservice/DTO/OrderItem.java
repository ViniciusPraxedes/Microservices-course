package com.example.emailservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    private String itemCode;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
}
