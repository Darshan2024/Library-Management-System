package edu.dugale.LibraryManagementSystem.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "waitlist")
public class WaitlistEntry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private Long bookId;
    @Column(nullable = false) private Long userId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private boolean active = true;

    public WaitlistEntry() {}
    public WaitlistEntry(Long userId, Long bookId) {
        this.userId = userId; this.bookId = bookId;
    }

    public Long getId() { return id; }
    public Long getBookId() { return bookId; }
    public Long getUserId() { return userId; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
