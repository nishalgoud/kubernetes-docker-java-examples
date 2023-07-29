package com.pluralsight.deployingspringframework6.repository;

import com.pluralsight.deployingspringframework6.model.Book;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BooksRepository extends CrudRepository<Book, Long> {

    List<Book> findAll();
}
