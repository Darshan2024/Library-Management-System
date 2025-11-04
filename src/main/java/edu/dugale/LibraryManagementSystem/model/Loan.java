package edu.dugale.LibraryManagementSystem.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "loan")
public class Loan {

    // ---- config knobs (optional defaults) ----
    private static final int DEFAULT_LOAN_DAYS = 14;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long bookId;

    // When the loan was created
    @Column(nullable = false)
    private LocalDate borrowedAt = LocalDate.now();

    // When the book is due (nullable until set; will be auto-filled @PrePersist if null)
    @Column
    private LocalDate dueDate;

    // How many times this loan has been extended
    @Column(nullable = false)
    private Integer renewals = 0;

    // Null means still active; non-null means returned on this date
    @Column
    private LocalDate returnedAt;

    public Loan() {}

    public Loan(Long userId, Long bookId) {
        this.userId = userId;
        this.bookId = bookId;
        this.borrowedAt = LocalDate.now();
    }

    // Ensure we always have a dueDate on first persist 
    @PrePersist
    private void onCreate() {
        if (dueDate == null) {
            dueDate = borrowedAt.plusDays(DEFAULT_LOAN_DAYS);
        }
    }

    // ---------- convenience methods ----------

    /** Active = not yet returned. */
    @Transient
    public boolean isActive() {
        return returnedAt == null;
    }

    /** Mark the loan as returned today. */
    public void setReturnedNow() {
        this.returnedAt = LocalDate.now();
    }

    /** Add days to due date (used by “extend”). */
    public void extendByDays(int days) {
        LocalDate base = (dueDate == null) ? LocalDate.now() : dueDate;
        this.dueDate = base.plusDays(days);
        this.renewals = (this.renewals == null ? 0 : this.renewals) + 1;
    }

    // ---------- getters / setters ----------

    public Long getId() { return id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public LocalDate getBorrowedAt() { return borrowedAt; }
    public void setBorrowedAt(LocalDate borrowedAt) { this.borrowedAt = borrowedAt; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Integer getRenewals() { return renewals; }
    public void setRenewals(Integer renewals) { this.renewals = renewals; }

    public LocalDate getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDate returnedAt) { this.returnedAt = returnedAt; }
}
