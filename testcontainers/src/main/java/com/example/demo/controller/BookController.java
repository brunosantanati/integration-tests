package com.example.demo.controller;

import com.example.demo.entity.Book;
import com.example.demo.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/*
This example is based on this tutorial:
https://1kevinson.com/integration-testing-with-springboot-docker-and-tests-containers/
Code on GitHub:
https://github.com/1kevinson/BLOG-TUTORIALS/tree/master/Java/testcontainers

You must run 'docker login' before running the integration test that uses Testcontainers
 */

@RestController
@RequiredArgsConstructor
public class BookController {

    private final BookService service;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/create/book")
    public Book createTodo(@Valid @RequestBody Book book) {
        return this.service.saveBook(book);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/fetch/books")
    public List<Book> createTodo() {
        return this.service.getBooks();
    }
}
