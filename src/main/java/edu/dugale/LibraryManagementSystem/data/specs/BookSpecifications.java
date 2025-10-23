package edu.dugale.LibraryManagementSystem.data.specs;

import edu.dugale.LibraryManagementSystem.model.Book;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class BookSpecifications {

    public static Specification<Book> containsText(String q) {
        if (q == null || q.isBlank()) return null;
        String like = "%" + q.trim().toLowerCase() + "%";
        return (root, cq, cb) -> cb.or(
            cb.like(cb.lower(root.get("title")), like),
            cb.like(cb.lower(root.get("authors")), like),
            cb.like(cb.lower(root.get("isbn")), like)
        );
    }

    public static Specification<Book> authorContains(String author) {
        if (author == null || author.isBlank()) return null;
        String like = "%" + author.trim().toLowerCase() + "%";
        return (root, cq, cb) -> cb.like(cb.lower(root.get("authors")), like);
    }

    public static Specification<Book> available(Boolean onlyAvailable) {
        if (onlyAvailable == null || !onlyAvailable) return null;
        return (root, cq, cb) -> cb.isTrue(root.get("available"));
    }

    public static Specification<Book> publishedBetween(Integer fromYear, Integer toYear) {
        if (fromYear == null && toYear == null) return null;

        LocalDate from = (fromYear != null) ? LocalDate.of(fromYear, 1, 1) : LocalDate.of(0, 1, 1);
        LocalDate to   = (toYear != null)   ? LocalDate.of(toYear, 12, 31) : LocalDate.of(9999, 12, 31);

        return (root, cq, cb) -> cb.between(root.get("published"), from, to);
    }
}
