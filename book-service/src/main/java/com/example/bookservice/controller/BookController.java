package com.example.bookservice.controller;

import com.example.bookservice.model.BookRequest;
import com.example.bookservice.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/book")
public class BookController {
    private final BookService bookService;
    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllBooks(){
        return bookService.getAllBooks();
    }
    @PostMapping("/create")
    public ResponseEntity<?> createBook(@RequestBody BookRequest request){
        return bookService.createBook(request);
    }

    @GetMapping("/getBook")
    public ResponseEntity<?> getBookByItemCode(@RequestParam String itemCode){
        return bookService.getBookByItemCode(itemCode);
    }

}
