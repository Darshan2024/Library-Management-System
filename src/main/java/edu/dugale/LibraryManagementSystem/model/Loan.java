package edu.dugale.LibraryManagementSystem.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long bookId;

    @Column(nullable = false)
    private LocalDate borrowedAt = LocalDate.now();

    public Loan() {
    }

    public Loan(Long userId, Long bookId) {
        this.userId = userId;
        this.bookId = bookId;
        this.borrowedAt = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getBookId() {
        return bookId;
    }

    public LocalDate getBorrowedAt() {
        return borrowedAt;
    }
}
