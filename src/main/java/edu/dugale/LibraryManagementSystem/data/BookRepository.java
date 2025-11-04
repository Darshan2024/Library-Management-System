// src/main/java/edu/dugale/LibraryManagementSystem/data/BookRepository.java
package edu.dugale.LibraryManagementSystem.data;

import edu.dugale.LibraryManagementSystem.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface BookRepository
        extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    List<Book> findByTitleContainingIgnoreCase(String q);

    List<Book> findByAuthorsContainingIgnoreCase(String q);

    List<Book> findByTitleContainingIgnoreCaseOrAuthorsContainingIgnoreCaseOrIsbnContainingIgnoreCase(
            String titlePart, String authorsPart, String isbnPart);
}
