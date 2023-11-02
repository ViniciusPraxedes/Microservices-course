package com.example.orderservice.DTO;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    private String orderNumber;

    @NotBlank(message = "First name is required")
    private String firstname;

    @NotBlank(message = "Last name is required")
    private String lastname;

    @Email(message = "Invalid email address")
    private String email;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @NotNull(message = "Postcode is required")
    @Min(value = 1000, message = "Postcode must be at least 1000")
    @Max(value = 9999, message = "Postcode must be at most 9999")
    private Integer postcode;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotEmpty(message = "Order items cannot be empty")
    private List<OrderItemDTO> orderItemsDTO;
}
