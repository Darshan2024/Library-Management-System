package edu.dugale.LibraryManagementSystem.controllers;

import edu.dugale.LibraryManagementSystem.data.BookRepository;
import edu.dugale.LibraryManagementSystem.data.LoanRepository;
import edu.dugale.LibraryManagementSystem.data.UserRepository;
import edu.dugale.LibraryManagementSystem.model.Book;
import edu.dugale.LibraryManagementSystem.model.Loan;
import edu.dugale.LibraryManagementSystem.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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

    @GetMapping("/books")
    public String books(@RequestParam(value = "q", required = false) String q,
                        Model model, HttpSession session) {
        Object uname = session.getAttribute("username");
        if (uname != null) model.addAttribute("username", uname.toString());

        boolean hasQuery = q != null && !q.trim().isEmpty();
        var results = hasQuery
                ? bookRepository
                    .findByTitleContainingIgnoreCaseOrAuthorsContainingIgnoreCaseOrIsbnContainingIgnoreCase(q, q, q)
                : Collections.emptyList();

        model.addAttribute("books", results);
        model.addAttribute("q", q == null ? "" : q.trim());
        model.addAttribute("hasQuery", hasQuery);
        model.addAttribute("count", results.size());
        return "books";
    }

    @PostMapping("/books/{id}/borrow")
    public String borrowBook(@PathVariable("id") Long id, HttpSession session) {
        Optional<Book> opt = bookRepository.findById(id);
        if (opt.isEmpty()) return "redirect:/books";
        Book b = opt.get();
        if (!b.getAvailable()) return "redirect:/books";

        String username = (String) session.getAttribute("username");
        if (username == null || username.isBlank()) return "redirect:/signin";

        User user = userRepository.findByName(username).orElseGet(() -> userRepository.save(new User(username)));;
        loanRepository.save(new Loan(user.getId(), b.getId()));
        b.setAvailable(false);
        bookRepository.save(b);

        return "redirect:/books";
    }
}
