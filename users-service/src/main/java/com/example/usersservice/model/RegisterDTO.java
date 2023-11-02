package com.example.usersservice.model;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterDTO {
    @NotBlank(message = "First name is mandatory")
    @Size(min = 2, max = 15, message = "First name must be between 2 and 15 characters")
    private String firstname;

    @NotBlank(message = "Last name is mandatory")
    @Size(min = 2, max = 15, message = "Last name must be between 2 and 15 characters")
    private String lastname;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Invalid email address")
    private String email;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 6, max = 15, message = "Password must be between 6 and 15 characters")
    private String password;

    @NotBlank(message = "Address is mandatory")
    @Size(min = 2, max = 30, message = "Address must be between 2 and 30 characters")
    private String address;

    @NotBlank(message = "City is mandatory")
    private String city;

    @NotNull(message = "Postcode is mandatory")
    @Min(value = 10000, message = "Postcode must be at least 5 digits")
    @Max(value = 99999, message = "Postcode must be at most 5 digits")
    private Integer postcode;

    private String phoneNumber;
}
