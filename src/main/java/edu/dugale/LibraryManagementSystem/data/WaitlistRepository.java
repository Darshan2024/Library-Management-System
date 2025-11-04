package edu.dugale.LibraryManagementSystem.data;

import edu.dugale.LibraryManagementSystem.model.WaitlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WaitlistRepository extends JpaRepository<WaitlistEntry, Long> {
    boolean existsByBookIdAndUserIdAndActiveTrue(Long bookId, Long userId);
    long countByBookIdAndActiveTrue(Long bookId);
    Optional<WaitlistEntry> findFirstByBookIdAndActiveTrueOrderByCreatedAtAsc(Long bookId);
    List<WaitlistEntry> findByUserIdAndActiveTrueOrderByCreatedAtAsc(Long userId);
    Optional<WaitlistEntry> findByIdAndUserIdAndActiveTrue(Long id, Long userId);
}
