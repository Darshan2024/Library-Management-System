// src/main/java/edu/dugale/LibraryManagementSystem/controllers/BookController.java
package edu.dugale.LibraryManagementSystem.controllers;

import edu.dugale.LibraryManagementSystem.data.BookRepository;
import edu.dugale.LibraryManagementSystem.data.LoanRepository;
import edu.dugale.LibraryManagementSystem.data.UserRepository;
import edu.dugale.LibraryManagementSystem.data.specs.BookSpecifications;
import edu.dugale.LibraryManagementSystem.model.Book;
import edu.dugale.LibraryManagementSystem.model.Loan;
import edu.dugale.LibraryManagementSystem.model.User;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import static org.springframework.data.jpa.domain.Specification.where;

@Controller
public class BookController {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final LoanRepository loanRepository;

    public BookController(BookRepository bookRepository,
                          UserRepository userRepository,
                          LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.loanRepository = loanRepository;
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return null;
        return auth.getName();
    }
    private User currentUserOrThrow() {
        String username = currentUsername();
        if (username == null) throw new IllegalStateException("Not authenticated");
        return userRepository.findByName(username)
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + username));
    }

    @GetMapping("/")
    public String books(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "available", required = false) Boolean onlyAvailable,
            @RequestParam(value = "yearFrom", required = false) Integer yearFrom,
            @RequestParam(value = "yearTo", required = false) Integer yearTo,
            @RequestParam(value = "sort", required = false, defaultValue = "relevance") String sort,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "25") int size,
            Model model) {

        String uname = currentUsername();
        if (uname != null) model.addAttribute("username", uname);

        // Build spec
        Specification<Book> spec = Specification.where(null);

        Specification<Book> s1 = BookSpecifications.containsText(q);
        if (s1 != null) spec = spec.and(s1);

        Specification<Book> s2 = BookSpecifications.authorContains(author);
        if (s2 != null) spec = spec.and(s2);

        Specification<Book> s3 = BookSpecifications.available(onlyAvailable);
        if (s3 != null) spec = spec.and(s3);

        Specification<Book> s4 = BookSpecifications.publishedBetween(yearFrom, yearTo);
        if (s4 != null) spec = spec.and(s4);

        // Sorting: "newest" or default
        Sort springSort = switch (sort) {
            case "newest" -> Sort.by(Sort.Direction.DESC, "published");
            case "title"  -> Sort.by(Sort.Direction.ASC, "title");
            default       -> Sort.unsorted(); // "relevance" placeholder
        };

        PageRequest pageable = PageRequest.of(page, size, springSort);
        Page<Book> results = bookRepository.findAll(spec, pageable);

        model.addAttribute("books", results.getContent());
        model.addAttribute("page", results);
        model.addAttribute("count", results.getTotalElements());

        // echo filters back to the view
        model.addAttribute("q", q == null ? "" : q.trim());
        model.addAttribute("author", author == null ? "" : author.trim());
        model.addAttribute("available", onlyAvailable != null && onlyAvailable);
        model.addAttribute("yearFrom", yearFrom);
        model.addAttribute("yearTo", yearTo);
        model.addAttribute("sort", sort);

        boolean hasQuery =
        (q != null && !q.isBlank()) ||
        (author != null && !author.isBlank()) ||
        (onlyAvailable != null && onlyAvailable) ||
        (yearFrom != null) ||
        (yearTo != null);

        model.addAttribute("hasQuery", hasQuery);


        return "books";
    }

    @PostMapping("/books/{id}/borrow")
    public String borrowBook(@PathVariable("id") Long id) {
        var book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + id));
        if (Boolean.FALSE.equals(book.getAvailable())) return "redirect:/?error=unavailable";

        var user = currentUserOrThrow();
        loanRepository.save(new Loan(user.getId(), book.getId()));
        book.setAvailable(false);
        bookRepository.save(book);

        return "redirect:/?borrowed=" + id;
    }
}
