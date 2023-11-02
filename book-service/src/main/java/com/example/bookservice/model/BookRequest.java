package com.example.bookservice.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BookRequest {
    private String name;
    private String author;
    private Integer numberOfPages;
    private String itemCode;
    private Integer amountInStock;
    private BigDecimal price;
}
