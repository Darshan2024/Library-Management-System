package edu.dugale.LibraryManagementSystem.data;

import edu.dugale.LibraryManagementSystem.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByUserIdAndReturnedAtIsNull(Long userId);

    Optional<Loan> findByIdAndUserIdAndReturnedAtIsNull(Long id, Long userId);

    boolean existsByBookIdAndUserIdAndReturnedAtIsNull(Long bookId, Long userId);

    List<Loan> findByBookIdAndReturnedAtIsNull(Long bookId);
}
