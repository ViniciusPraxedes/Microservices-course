package com.example.emailservice.DTO;

import lombok.Data;

@Data
public class UserCreatedEvent {
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private String address;
    private String city;
    private Integer postcode;
    private String phoneNumber;
}
