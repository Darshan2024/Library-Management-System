package edu.dugale.LibraryManagementSystem.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users") 
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotNull
    @Size(min = 2, message = "Username must be at least 2 characters long")
    private String name;
    @ElementCollection
    private List<UUID> borrowedBooks;

    public User() {
        name="";
        borrowedBooks = new ArrayList<>();
    }

    public User(String name) {
        this.name = name;
        borrowedBooks = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<UUID> getBorrowedBooks() {
        return borrowedBooks;
    }

    public boolean returnBook(UUID bookId) {
        return borrowedBooks.remove(bookId);
    }

    public boolean borrowBook(UUID bookId) {
        return borrowedBooks.add(bookId);
    }
}