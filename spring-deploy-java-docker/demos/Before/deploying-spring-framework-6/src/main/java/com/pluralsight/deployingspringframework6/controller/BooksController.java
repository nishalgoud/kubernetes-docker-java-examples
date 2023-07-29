package com.pluralsight.deployingspringframework6.controller;

import com.pluralsight.deployingspringframework6.model.Book;
import com.pluralsight.deployingspringframework6.service.BooksService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BooksController {

    private BooksService booksService;

    public BooksController(BooksService booksService) {
        this.booksService = booksService;
    }

    @GetMapping
    public List<Book> getAllBooks() {
        return booksService.getAllBooks();
    }

    @GetMapping("/{id}")
    public Book getBookById(@PathVariable Long id) {
        return booksService.getBookById(id);
    }

    @PostMapping
    public Book createBook(@RequestBody Book book) {
        return booksService.createBook(book);
    }

    @DeleteMapping("/{id}")
    public Book deleteBook(@PathVariable Long id) {
        return booksService.deleteBook(id);
    }

    @PostMapping("/persist")
    public Book persistBook(@RequestBody Book book) {
        return booksService.persistBook(book);
    }
}
