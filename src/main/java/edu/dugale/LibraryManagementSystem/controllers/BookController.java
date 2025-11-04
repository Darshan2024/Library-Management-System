// src/main/java/edu/dugale/LibraryManagementSystem/controllers/BookController.java
package edu.dugale.LibraryManagementSystem.controllers;

import edu.dugale.LibraryManagementSystem.data.BookRepository;
import edu.dugale.LibraryManagementSystem.data.LoanRepository;
import edu.dugale.LibraryManagementSystem.data.UserRepository;
import edu.dugale.LibraryManagementSystem.data.WaitlistRepository;
import edu.dugale.LibraryManagementSystem.data.specs.BookSpecifications;
import edu.dugale.LibraryManagementSystem.model.Book;
import edu.dugale.LibraryManagementSystem.model.Loan;
import edu.dugale.LibraryManagementSystem.model.User;
import edu.dugale.LibraryManagementSystem.model.WaitlistEntry;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class BookController {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final WaitlistRepository waitlistRepository;

    // policy knobs
    private static final int EXTEND_DAYS = 14;
    private static final int MAX_RENEWALS = 2;

    public BookController(BookRepository bookRepository,
            UserRepository userRepository,
            LoanRepository loanRepository,
            WaitlistRepository waitlistRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.loanRepository = loanRepository;
        this.waitlistRepository = waitlistRepository;
    }

    // ---------- helpers ----------
    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal()))
            return null;
        return auth.getName();
    }

    private User currentUserOrThrow() {
        String username = currentUsername();
        if (username == null)
            throw new IllegalStateException("Not authenticated");
        return userRepository.findByName(username)
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + username));
    }

    // ---------- catalog ----------
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
         if (uname != null) {
        model.addAttribute("username", uname);
        var user = userRepository.findByName(uname).orElse(null);
        if (user != null) {
            // get IDs of books currently borrowed by this user
            var myActiveBookIds = loanRepository
                    .findByUserIdAndReturnedAtIsNull(user.getId())
                    .stream()
                    .map(Loan::getBookId)
                    .toList();
            model.addAttribute("myActiveBookIds", myActiveBookIds);
        }
    }
        Specification<Book> spec = Specification.where(null);
        var s1 = BookSpecifications.containsText(q);
        if (s1 != null)
            spec = spec.and(s1);
        var s2 = BookSpecifications.authorContains(author);
        if (s2 != null)
            spec = spec.and(s2);
        var s3 = BookSpecifications.available(onlyAvailable);
        if (s3 != null)
            spec = spec.and(s3);
        var s4 = BookSpecifications.publishedBetween(yearFrom, yearTo);
        if (s4 != null)
            spec = spec.and(s4);

        Sort springSort = switch (sort) {
            case "newest" -> Sort.by(Sort.Direction.DESC, "published");
            case "title" -> Sort.by(Sort.Direction.ASC, "title");
            default -> Sort.unsorted();
        };

        PageRequest pageable = PageRequest.of(page, size, springSort);
        Page<Book> results = bookRepository.findAll(spec, pageable);

        var holdCounts = results.getContent().stream()
                .collect(Collectors.toMap(Book::getId, b -> waitlistRepository.countByBookIdAndActiveTrue(b.getId())));
        model.addAttribute("holdCounts", holdCounts);

        model.addAttribute("books", results.getContent());
        model.addAttribute("page", results);
        model.addAttribute("count", results.getTotalElements());

        model.addAttribute("q", q == null ? "" : q.trim());
        model.addAttribute("author", author == null ? "" : author.trim());
        model.addAttribute("available", onlyAvailable != null && onlyAvailable);
        model.addAttribute("yearFrom", yearFrom);
        model.addAttribute("yearTo", yearTo);
        model.addAttribute("sort", sort);

        boolean hasQuery = (q != null && !q.isBlank()) ||
                (author != null && !author.isBlank()) ||
                (onlyAvailable != null && onlyAvailable) ||
                (yearFrom != null) ||
                (yearTo != null);
        model.addAttribute("hasQuery", hasQuery);

        return "books";
    }

    @PostMapping("/books/{id}/borrow")
    @Transactional
    public String borrowBook(@PathVariable("id") Long id) {
        var book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + id));
        if (Boolean.FALSE.equals(book.getAvailable()))
            return "redirect:/?error=unavailable";

        var user = currentUserOrThrow();

        // save loan with FK ids
        loanRepository.save(new Loan(user.getId(), book.getId()));

        // make the book unavailable
        book.setAvailable(false);
        bookRepository.save(book);

        return "redirect:/?borrowed=" + id;
    }

    @PostMapping("/books/{id}/hold")
    @Transactional
    public String placeHold(@PathVariable("id") Long bookId) {
        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));
        if (Boolean.TRUE.equals(book.getAvailable())) {
            return "redirect:/?error=alreadyAvailable";
        }
        var user = currentUserOrThrow();
        if (waitlistRepository.existsByBookIdAndUserIdAndActiveTrue(bookId, user.getId())) {
            return "redirect:/?error=dupHold";
        }
        // If user already has an active loan for this book, no need to hold
        if (loanRepository.existsByBookIdAndUserIdAndReturnedAtIsNull(bookId, user.getId())) {
            return "redirect:/?error=alreadyBorrowing";
        }
        waitlistRepository.save(new WaitlistEntry(user.getId(), bookId));
        return "redirect:/?held=" + bookId;
    }

    @PostMapping("/waitlist/{entryId}/cancel")
    @Transactional
    public String cancelHold(@PathVariable Long entryId) {
        var user = currentUserOrThrow();
        var entry = waitlistRepository.findByIdAndUserIdAndActiveTrue(entryId, user.getId())
                .orElse(null);
        if (entry == null)
            return "redirect:/?error=notfound";
        entry.setActive(false);
        waitlistRepository.save(entry);
        return "redirect:/?holdCancelled=" + entryId;
    }

    
@PostMapping("/my/books/{loanId}/return")
@Transactional
public String returnOne(@PathVariable Long loanId) {
    var user = currentUserOrThrow();
    var opt = loanRepository.findByIdAndUserIdAndReturnedAtIsNull(loanId, user.getId());
    if (opt.isEmpty()) return "redirect:/my/books?error=notfound";

    Loan loan = opt.get();
    loan.setReturnedNow();
    loanRepository.save(loan);

    Long bookId = loan.getBookId();

    // Find next in queue
    var nextOpt = waitlistRepository.findFirstByBookIdAndActiveTrueOrderByCreatedAtAsc(bookId);
    if (nextOpt.isPresent()) {
        var next = nextOpt.get();
        // create a loan for next user and keep the book unavailable
        loanRepository.save(new Loan(next.getUserId(), bookId));
        next.setActive(false);
        waitlistRepository.save(next);
        // ensure the book stays unavailable
        bookRepository.findById(bookId).ifPresent(b -> {
            b.setAvailable(false);
            bookRepository.save(b);
        });
    } else {
        // no one waiting → make it available
        bookRepository.findById(bookId).ifPresent(b -> {
            b.setAvailable(true);
            bookRepository.save(b);
        });
    }
    return "redirect:/my/books?returned=" + loanId;
}

}
