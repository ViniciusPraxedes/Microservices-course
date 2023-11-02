package com.example.orderservice.controller;


import com.example.orderservice.DTO.OrderRequest;
import com.example.orderservice.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    //@CircuitBreaker(name = "order-controller-placeOrder", fallbackMethod = "fallbackMethod")
    @PostMapping("/placeOrder")
    public ResponseEntity<?> placeOrder(@Valid @RequestBody OrderRequest orderRequest) {
        return orderService.createOrder(orderRequest);
    }
    public ResponseEntity<?> fallbackMethod(OrderRequest orderRequest, RuntimeException runtimeException){
        return new ResponseEntity<>("Oops, something went wrong, try again later", HttpStatus.GATEWAY_TIMEOUT);
    }
}