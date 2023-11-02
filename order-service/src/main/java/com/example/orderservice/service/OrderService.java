package com.example.orderservice.service;

import com.example.orderservice.DTO.*;
import com.example.orderservice.model.*;
import com.example.orderservice.repository.OrderRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final RabbitTemplate rabbitTemplate;
    @Autowired
    public OrderService(OrderRepository orderRepository, WebClient.Builder webClientBuilder, RabbitTemplate rabbitTemplate) {
        this.orderRepository = orderRepository;
        this.webClientBuilder = webClientBuilder;
        this.rabbitTemplate = rabbitTemplate;
    }

    public ResponseEntity<?> createOrder(OrderRequest request){

        //Generates random order number
        request.setOrderNumber(generateRandomOrderNumber());

        //Although the possibility is low, this will check if the order number already exists
        if (orderRepository.findByOrderNumber(request.getOrderNumber()).isPresent()){
            return new ResponseEntity<>("Order number already exists", HttpStatus.BAD_REQUEST);
        }

        //Creates a list of items that belongs to the order and set it to the items from the request
        List<OrderItem> orderItems = request.getOrderItemsDTO()
                .stream()
                .map(this::mapToDto)
                .toList();

        //Creates order and sets the products within the order to the products from the request
        Order order = Order.builder()
                .orderNumber(generateRandomOrderNumber())
                .orderItems(orderItems).build();

        //Gets the itemCode from the items within the order and put them into a list
        List<String> itemCodes = order.getOrderItems().stream().map(OrderItem::getItemCode).toList();

        //Asks the inventory if all items in the order are in the inventory
        InventoryResponse[] inventoryResponses = webClientBuilder.build().get()
                .uri("http://inventory-service/api/v1/inventory/isItemInStockManyItems", uriBuilder -> uriBuilder.queryParam("itemCodes", itemCodes).build())
                .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        //If all items in the order are in stock then it will return true
        boolean allProductsInStock = Arrays.stream(inventoryResponses).allMatch(InventoryResponse::isInStock);

        //Checks which items are out of stock
        List<InventoryResponse> filteredResponses = Arrays.asList(inventoryResponses)
                .stream()
                .filter(response -> response.getQuantity() <= 0)
                .collect(Collectors.toList());

        //Get the quantity of the items in the order, so that it can be reduced once the order has been placed
        List<Integer> quantity = order.getOrderItems().stream().map(OrderItem::getQuantity).toList();

        if (allProductsInStock){

            order.setFirstname(request.getFirstname());
            order.setLastname(request.getLastname());
            order.setEmail(request.getEmail());
            order.setPhoneNumber(request.getPhoneNumber());
            order.setAddress(request.getAddress());
            order.setPostcode(request.getPostcode());
            order.setCity(request.getCity());

            //Saves the order to the database
            orderRepository.save(order);

            //RabbitMQ routing key
            String routingKey = "orders.v1.order-created";

            //Creates an event to be sent with rabbitMQ
            OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent();

            //Maps all variables within order to orderCreatedEvent
            BeanUtils.copyProperties(order, orderCreatedEvent);

            //Send the event to RabbitMQ and furthermost to the email service
           rabbitTemplate.convertAndSend(routingKey, orderCreatedEvent);

            //Reduces the quantity of the items in the inventory once the order is placed
            webClientBuilder.build().post()
                    .uri("http://inventory-service/api/v1/inventory/decreaseQuantityManyItems", uriBuilder -> uriBuilder.queryParam("itemCodes", itemCodes).queryParam("quantities", quantity).build())
                    .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
                    .retrieve()
                    .toBodilessEntity()
                    .block();


            return new ResponseEntity<>("Order placed: " + order, HttpStatus.OK);
        }else {
            return new ResponseEntity<>("Items out of stock: "+filteredResponses.toString(), HttpStatus.BAD_REQUEST);
        }
    }


    //Sets each value of the item inside the list to the values of the dto
    //This function is needed in order for the createOrder() function to work properly
    private OrderItem mapToDto(OrderItemDTO orderItemDTO) {
        OrderItem orderItem = new OrderItem();
        orderItem.setPrice(orderItemDTO.getPrice());
        orderItem.setQuantity(orderItemDTO.getQuantity());
        orderItem.setItemCode(orderItemDTO.getItemCode());
        orderItem.setProductName(orderItemDTO.getProductName());
        return orderItem;
    }

    //Generates a random order number using letters and numbers
    //This method is needed so createOrder can function properly
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERS = "0123456789";
    private static final Random random = new Random();
    public static String generateRandomOrderNumber() {
        StringBuilder randomString = new StringBuilder();

        String source = LETTERS + NUMBERS;

        for (int i = 0; i < 9; i++) {
            char randomChar = source.charAt(random.nextInt(source.length()));
            randomString.append(randomChar);
        }

        return randomString.toString();
    }


}
