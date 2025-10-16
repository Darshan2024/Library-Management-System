package edu.dugale.LibraryManagementSystem.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    private String isbn;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String authors;

    @Column(length = 2000)
    private String description;

    private LocalDate published;

    private boolean available = true;

    public Book() {
    
    }

    public Book(String isbn, String title, String authors, String description, LocalDate published, boolean available){

        this.isbn = isbn;
        this.title = title;
        this.authors = authors;
        this.description = description;
        this.published = published;
        this.available = available;
        }
        

    public Long getId(){
        return id;
    }

    public String getIsbn(){
        return isbn;
    }

    public String getTitle(){
        return title;
    }     

    public String getAuthors(){
        return authors;
    }

    public String getDescription(){
        return description;
    }

    public LocalDate getPublished(){
        return published;
    }

    public boolean getAvailable(){
        return available;
    }

    public void setAvailable(boolean newAvailability) {
        available = newAvailability;
    }
}


