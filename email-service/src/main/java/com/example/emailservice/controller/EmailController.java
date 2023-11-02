package com.example.emailservice.controller;

import com.example.emailservice.DTO.EmailDTO;
import com.example.emailservice.model.Email;
import com.example.emailservice.service.EmailService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
public class EmailController {
    private final EmailService emailService;
    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @CircuitBreaker(name = "email-controller-send", fallbackMethod = "fallbackMethod")
    @PostMapping("/send")
    public ResponseEntity<?> sendEmail(@RequestBody @Valid EmailDTO emailDTO){
        return emailService.sendEmail(emailDTO);
    }
    public ResponseEntity<?> fallbackMethod(EmailDTO emailDTO, RuntimeException runtimeException){
        return new ResponseEntity<>("Oops, something went wrong, try again later", HttpStatus.GATEWAY_TIMEOUT);
    }
}
