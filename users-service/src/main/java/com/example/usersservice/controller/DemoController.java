package com.example.usersservice.controller;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/")
@AllArgsConstructor
public class DemoController {
    @GetMapping("/test")
    public String test(){
        try{
            return "test";
        }catch (Exception e){
            throw new ExpiredJwtException(null,null,"Jwt expired");
        }

    }
}