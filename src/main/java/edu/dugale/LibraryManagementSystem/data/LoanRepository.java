package edu.dugale.LibraryManagementSystem.data;

import edu.dugale.LibraryManagementSystem.model.Loan;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends CrudRepository<Loan, Long> {
    List<Loan> findByUserId(Long userId);

    List<Loan> findByBookId(Long bookId);
}
