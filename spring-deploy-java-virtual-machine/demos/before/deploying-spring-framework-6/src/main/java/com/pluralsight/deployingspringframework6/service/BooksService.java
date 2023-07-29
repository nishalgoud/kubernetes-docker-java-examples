package com.pluralsight.deployingspringframework6.service;

import com.pluralsight.deployingspringframework6.model.Book;
import com.pluralsight.deployingspringframework6.repository.BooksRepository;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;

@Service
public class BooksService {

    private static final String FILE_NAME = "/tmp/books.txt";

    private BooksRepository booksRepository;

    @SneakyThrows
    public BooksService(BooksRepository booksRepository) {
        this.booksRepository = booksRepository;
        if (!Files.exists(Paths.get(FILE_NAME)))
            Files.createFile(Paths.get(FILE_NAME));
    }

    public List<Book> getAllBooks() {
        return booksRepository.findAll();
    }

    public Book getBookById(Long id) {
        return booksRepository.findById(id).orElse(null);
    }

    public Book createBook(Book book) {
        return booksRepository.save(book);
    }

    public Book deleteBook(Long id) {
        Book book = booksRepository.findById(id).orElse(null);
        if(Objects.nonNull(book))
            booksRepository.deleteById(id);
        return book;
    }

    @SneakyThrows
    public Book persistBook(Book book) {
        String contentToAppend = String.format("%s,%s,%s\n",book.getId(),book.getTitle(), book.getAuthorName());

        Files.write(
                Paths.get(FILE_NAME),
                contentToAppend.getBytes(),
                StandardOpenOption.APPEND);
        return book;
    }
}
