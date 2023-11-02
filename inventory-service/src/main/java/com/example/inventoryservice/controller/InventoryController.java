package com.example.inventoryservice.controller;

import com.example.inventoryservice.model.ItemInTheInventory;
import com.example.inventoryservice.model.ItemInTheInventoryRequest;
import com.example.inventoryservice.model.ItemInTheInventoryResponse;
import com.example.inventoryservice.service.InventoryService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {
    private InventoryService inventoryService;
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @CircuitBreaker(name = "inventory-controller-isInStock", fallbackMethod = "fallbackIsInStock")
    @GetMapping("/isItemInStockManyItems")
    public List<ItemInTheInventoryResponse> isItemInStock(@RequestParam List<String> itemCodes){
        return inventoryService.isInStockManyItems(itemCodes);
    }
    @GetMapping("/isItemInStockSingleItem")
    public List<ItemInTheInventoryResponse> isItemInStock(@RequestParam String itemCode){
        return inventoryService.isItemInStockSingleItem(itemCode);
    }
    @PostMapping("/decreaseQuantitySingleItem")
    public ResponseEntity<?> decreaseQuantitySingleItem(@RequestParam String itemCode, @RequestParam Integer quantity){
        return inventoryService.decreaseQuantitySingleItem(itemCode,quantity);
    }
    @CircuitBreaker(name = "inventory-controller-reduceQuantity", fallbackMethod = "fallbackMethodReduceQuantity")
    @PostMapping("/decreaseQuantityManyItems")
    public ResponseEntity<?> decreaseQuantityManyItems(@RequestParam List<String> itemCodes, @RequestParam List<Integer> quantities){
        return inventoryService.decreaseQuantityManyItems(itemCodes, quantities);
    }
    @PostMapping("/increaseQuantitySingleItem")
    public ResponseEntity<?> increaseQuantitySingleItem(@RequestParam String itemCode, @RequestParam Integer quantity){
        return inventoryService.increaseQuantitySingleItem(itemCode,quantity);
    }
    @PostMapping("/increaseQuantityManyItems")
    public ResponseEntity<?> increaseQuantityManyItems(@RequestParam List<String> itemCodes, @RequestParam List<Integer> quantities){
        return inventoryService.increaseQuantityManyItems(itemCodes, quantities);
    }

    @PostMapping("/addItem")
    public ResponseEntity<?> addItem(@RequestBody ItemInTheInventoryRequest request){
        return inventoryService.addItem(request);
    }
    @PostMapping("/deleteItem")
    public ResponseEntity<?> removeItem(@RequestParam String itemCode){
        return inventoryService.deleteItem(itemCode);
    }
    @GetMapping("/getAll")
    public List<ItemInTheInventoryResponse> getAll(){
        return inventoryService.getAll();
    }
    @GetMapping("/getItem")
    public ItemInTheInventoryResponse getItem(@RequestParam String itemCode){
        return inventoryService.getItem(itemCode);
    }

    public ResponseEntity<?> fallbackMethodReduceQuantity(List<String> itemCode, List<Integer> quantity, RuntimeException runtimeException){
        return new ResponseEntity<>("Oops, something went wrong, try again later", HttpStatus.GATEWAY_TIMEOUT);
    }
    public ResponseEntity<?> fallbackMethodIsInStock(List<String> itemCode, RuntimeException runtimeException){
        return new ResponseEntity<>("Oops, something went wrong, try again later", HttpStatus.GATEWAY_TIMEOUT);
    }
}
