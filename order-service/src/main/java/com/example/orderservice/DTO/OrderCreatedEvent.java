package com.example.orderservice.DTO;

import com.example.orderservice.model.OrderItem;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent {
    private String orderNumber;

    private String firstname;

    private String lastname;

    private String email;

    private String address;

    private String city;

    private Integer postcode;

    private String phoneNumber;

    private List<OrderItem> orderItems;
}
