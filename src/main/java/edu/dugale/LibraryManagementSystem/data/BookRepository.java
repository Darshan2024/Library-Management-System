package edu.dugale.LibraryManagementSystem.data;

import org.springframework.data.repository.CrudRepository;
import edu.dugale.LibraryManagementSystem.model.Book;

import java.util.*;

public interface BookRepository extends CrudRepository<Book, Long> {
    List<Book> findByTitleContainingIgnoreCase(String q);

    List<Book> findByAuthorsContainingIgnoreCase(String q);

    List<Book> findByTitleContainingIgnoreCaseOrAuthorsContainingIgnoreCaseOrIsbnContainingIgnoreCase(
            String titlePart, String authorsPart, String isbnPart);
}