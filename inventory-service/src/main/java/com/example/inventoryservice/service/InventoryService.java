package com.example.inventoryservice.service;

import com.example.inventoryservice.model.ItemInTheInventory;
import com.example.inventoryservice.model.ItemInTheInventoryRequest;
import com.example.inventoryservice.model.ItemInTheInventoryResponse;
import com.example.inventoryservice.repository.ItemInTheInventoryRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryService {
    private ItemInTheInventoryRepository itemInTheInventoryRepository;

    public InventoryService(ItemInTheInventoryRepository itemInTheInventoryRepository) {
        this.itemInTheInventoryRepository = itemInTheInventoryRepository;
    }
    //Return list of items in the inventory
    //MustReturnBoolean
    @Transactional(readOnly = true)
    public List<ItemInTheInventoryResponse> isInStockManyItems(List<String> itemCodes){
        return itemInTheInventoryRepository.findByItemCodeIn(itemCodes).stream()
                .map(ItemInTheInventory ->
                        ItemInTheInventoryResponse.builder()
                                .itemCode(ItemInTheInventory.getItemCode())
                                .isInStock(ItemInTheInventory.getQuantity() > 0)
                                .quantity(ItemInTheInventory.getQuantity())
                                .build()).toList();
    }

    //Return item in the inventory
    //Must return boolean
    @Transactional(readOnly = true)
    public List<ItemInTheInventoryResponse> isItemInStockSingleItem(String itemCode) {
        Optional<ItemInTheInventory> itemInTheInventory = itemInTheInventoryRepository.findByItemCode(itemCode);

        if (itemInTheInventory.isEmpty()) {
            return Collections.emptyList(); // Return an empty list when item is not found
        }

        ItemInTheInventory item = itemInTheInventory.get();
        boolean isInStock = item.getQuantity() > 0;
        Integer quantity = item.getQuantity();

        ItemInTheInventoryResponse response = ItemInTheInventoryResponse.builder()
                .itemCode(item.getItemCode())
                .isInStock(isInStock)
                .quantity(quantity)
                .build();


        return Collections.singletonList(response); // Return a list containing the response

    }

    //Reduces the quantity of A LIST of item in the inventory
    public ResponseEntity<?> decreaseQuantityManyItems(List<String> itemCodes, List<Integer> amounts){
        List<ItemInTheInventory> items = itemInTheInventoryRepository.findByItemCodeIn(itemCodes);

        // Check if all items exist
        if (items.size() < itemCodes.size()) {
            return new ResponseEntity<>("One or more items do not exist in the inventory", HttpStatus.NOT_FOUND);
        }

        // Check if any item has zero quantity
        for (ItemInTheInventory item : items) {
            if (item.getQuantity() <= 0) {
                return new ResponseEntity<>("Item: " + item.getItemCode() + " has zero units in the inventory", HttpStatus.BAD_REQUEST);
            }
        }

        // Reduce quantities
        for (int i = 0; i < itemCodes.size(); i++) {
            itemInTheInventoryRepository.reduceQuantityByItemCode(itemCodes.get(i), amounts.get(i));
        }

        return new ResponseEntity<>("Quantities reduced successfully for items: " + itemCodes, HttpStatus.OK);
    }

    //Decreases the quantity of ONE SINGLE item in the inventory
    public ResponseEntity<?> decreaseQuantitySingleItem(String itemCode, Integer quantity){
        if (itemInTheInventoryRepository.findByItemCode(itemCode).isEmpty()){
            return new ResponseEntity<>("Item not found", HttpStatus.NOT_FOUND);
        }
        int actualQuantity = itemInTheInventoryRepository.findByItemCode(itemCode).get().getQuantity();
        int newQuantity = actualQuantity - quantity;

        if (newQuantity < 0){
            return new ResponseEntity<>("There is not enough of item: "+itemCode+" to be reduced, the actual quantity is: "+actualQuantity+" while you want to reduce: "+quantity, HttpStatus.BAD_REQUEST);
        }

        itemInTheInventoryRepository.setQuantityByItemCode(itemCode,newQuantity);

        return new ResponseEntity<>("Quantity for item: "+itemCode+" successfully decreased, new quantity is: "+newQuantity, HttpStatus.OK);
    }

    //Increase the quantity of ONE SINGLE item in the inventory
    public ResponseEntity<?> increaseQuantitySingleItem(String itemCode, Integer quantity){
        if (itemInTheInventoryRepository.findByItemCode(itemCode).isEmpty()){
            return new ResponseEntity<>("Item not found", HttpStatus.NOT_FOUND);
        }
        int actualQuantity = itemInTheInventoryRepository.findByItemCode(itemCode).get().getQuantity();
        int newQuantity = actualQuantity + quantity;

        itemInTheInventoryRepository.setQuantityByItemCode(itemCode,newQuantity);

        return new ResponseEntity<>("Quantity for item: "+itemCode+" successfully increased, new quantity is: "+newQuantity, HttpStatus.OK);
    }
    //Increases the quantity of A LIST of items in the inventory
    public ResponseEntity<?> increaseQuantityManyItems(List<String> itemCodes, List<Integer> amounts) {
        List<ItemInTheInventory> items = itemInTheInventoryRepository.findByItemCodeIn(itemCodes);

        // Check if all items exist
        if (items.size() < itemCodes.size()) {
            return new ResponseEntity<>("One or more items do not exist in the inventory", HttpStatus.NOT_FOUND);
        }

        // Increase quantities
        for (int i = 0; i < itemCodes.size(); i++) {
            ItemInTheInventory item = items.get(i);
            int newQuantity = item.getQuantity() + amounts.get(i);
            itemInTheInventoryRepository.setQuantityByItemCode(itemCodes.get(i), newQuantity);
        }

        return new ResponseEntity<>("Quantities increased successfully for items: " + itemCodes, HttpStatus.OK);
    }

    //Adds new item to the inventory
    public ResponseEntity<?> addItem(ItemInTheInventoryRequest request){
        if (itemInTheInventoryRepository.findByItemCode(request.getItemCode()).isPresent()){
            return new ResponseEntity<>("This item already exists in the inventory",HttpStatus.BAD_REQUEST);
        }
        ItemInTheInventory itemInTheInventory = new ItemInTheInventory();
        itemInTheInventory.setItemCode(request.getItemCode());
        itemInTheInventory.setQuantity(request.getQuantity());

        itemInTheInventoryRepository.save(itemInTheInventory);

        return new ResponseEntity<>("Item: "+request.getItemCode()+" successfully added", HttpStatus.OK);
    }
    //Deletes an item in the inventory
    public ResponseEntity<?> deleteItem(String itemCode){
        if (itemInTheInventoryRepository.findByItemCode(itemCode).isEmpty()){
            return new ResponseEntity<>("item not found",HttpStatus.NOT_FOUND);
        }

        ItemInTheInventory item = itemInTheInventoryRepository.findByItemCode(itemCode).get();
        itemInTheInventoryRepository.delete(item);

        return new ResponseEntity<>("Item: "+itemCode+" successfully deleted", HttpStatus.OK);
    }

    public List<ItemInTheInventoryResponse> getAll() {
        List<ItemInTheInventory> itemList = itemInTheInventoryRepository.findAll();
        List<ItemInTheInventoryResponse> responseList = new ArrayList<>();

        for (ItemInTheInventory item : itemList) {
            ItemInTheInventoryResponse response = ItemInTheInventoryResponse.builder()
                    .itemCode(item.getItemCode())
                    .isInStock(item.getQuantity() > 0)
                    .quantity(item.getQuantity())
                    .build();

            responseList.add(response);
        }

        return responseList;
    }
    public ItemInTheInventoryResponse getItem(String itemCode){
        if (itemInTheInventoryRepository.findByItemCode(itemCode).isEmpty()){
            return ItemInTheInventoryResponse.builder().build();
        }
        ItemInTheInventory item = itemInTheInventoryRepository.findByItemCode(itemCode).get();
        ItemInTheInventoryResponse response = ItemInTheInventoryResponse.builder()
                .itemCode(item.getItemCode())
                .isInStock(item.getQuantity() > 0)
                .quantity(item.getQuantity())
                .build();
        return response;
    }




















}
