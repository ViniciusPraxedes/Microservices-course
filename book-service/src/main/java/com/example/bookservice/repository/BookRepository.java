package com.example.bookservice.repository;

import com.example.bookservice.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface BookRepository extends JpaRepository <Book, Integer> {
    Optional<Book> findByName(String bookName);
    Optional<Book> findByItemCode(String itemCode);
}
