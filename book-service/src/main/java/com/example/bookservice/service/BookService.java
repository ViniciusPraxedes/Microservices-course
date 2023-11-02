package com.example.bookservice.service;


import com.example.bookservice.model.Book;
import com.example.bookservice.model.BookRequest;
import com.example.bookservice.model.BookResponse;
import com.example.bookservice.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
public class BookService {
    private final BookRepository bookRepository;
    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public ResponseEntity<?> createBook(BookRequest bookRequest){
        if (bookRepository.findByName(bookRequest.getName()).isPresent()){
            return new ResponseEntity<>("Book already exists",HttpStatus.BAD_GATEWAY);
        }
        Book book = Book.builder()
                .name(bookRequest.getName())
                .author(bookRequest.getAuthor())
                .price(bookRequest.getPrice())
                .numberOfPages(bookRequest.getNumberOfPages())
                .amountInStock(bookRequest.getAmountInStock())
                .itemCode(bookRequest.getItemCode())
                .build();

        bookRepository.save(book);

        return new ResponseEntity<>("Book created", HttpStatus.CREATED);
    }
    public ResponseEntity<?> getAllBooks(){
        List<Book> books = bookRepository.findAll();

        return new ResponseEntity<>(books.stream().map(this::mapToBookResponse).toList(),HttpStatus.OK);
    }
    public ResponseEntity<?> getBookByItemCode(String itemCode){
        if (bookRepository.findByItemCode(itemCode).isEmpty()){
            return new ResponseEntity<>("Book not found", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(bookRepository.findByItemCode(itemCode).get(), HttpStatus.OK);
    }

    private BookResponse mapToBookResponse(Book book){
        return BookResponse.builder()
                .name(book.getName())
                .author(book.getAuthor())
                .numberOfPages(book.getNumberOfPages())
                .price(book.getPrice())
                .itemCode(book.getItemCode())
                .build();
    }


}
