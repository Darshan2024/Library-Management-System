package edu.dugale.LibraryManagementSystem.controllers;

import edu.dugale.LibraryManagementSystem.data.BookRepository;
import edu.dugale.LibraryManagementSystem.data.LoanRepository;
import edu.dugale.LibraryManagementSystem.data.UserRepository;
import edu.dugale.LibraryManagementSystem.data.WaitlistRepository;
import edu.dugale.LibraryManagementSystem.model.Book;
import edu.dugale.LibraryManagementSystem.model.Loan;
import edu.dugale.LibraryManagementSystem.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/my/books")
public class MyBooksController {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final WaitlistRepository waitlistRepository;

    // simple policy knobs
    private static final int EXTEND_DAYS = 14; // each extension adds 14 days
    private static final int MAX_RENEWALS = 2; // cap extensions

    public MyBooksController(LoanRepository loanRepository,
            UserRepository userRepository,
            BookRepository bookRepository,
            WaitlistRepository waitlistRepository) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.waitlistRepository = waitlistRepository;
    }

record LoanRow(edu.dugale.LibraryManagementSystem.model.Loan loan,
               edu.dugale.LibraryManagementSystem.model.Book book) {}

@GetMapping
public String myBooks(Model model, Authentication auth) {
    var user = userRepository.findByName(auth.getName()).orElse(null);
    if (user == null) return "redirect:/?error=unauth";
    Long userId = user.getId();

    var loans = loanRepository.findByUserIdAndReturnedAtIsNull(userId);
    var books = bookRepository.findAllById(loans.stream().map(Loan::getBookId).toList())
                              .stream().collect(java.util.stream.Collectors.toMap(b->b.getId(), b->b));
    var rows = loans.stream()
        .map(l -> new LoanRow(l, books.get(l.getBookId())))
        .toList();

    model.addAttribute("rows", rows);
    model.addAttribute("canExtendAll", loans.stream().anyMatch(this::canExtend));
    return "myBooks";
}


@PostMapping("/{loanId}/extend")
@Transactional
public String extendOne(@PathVariable Long loanId, Authentication auth) {
    var user = userRepository.findByName(auth.getName()).orElse(null);
    if (user == null) return "redirect:/?error=unauth";
    Long userId = user.getId();

    var opt = loanRepository.findByIdAndUserIdAndReturnedAtIsNull(loanId, userId);
    if (opt.isEmpty()) return "redirect:/my/books?error=notfound";

    var loan = opt.get();
    if (!canExtend(loan)) return "redirect:/my/books?error=cannotExtend";

    loan.extendByDays(14);   
    loanRepository.save(loan);
    return "redirect:/my/books?extended=" + loanId;
}

@PostMapping("/extend-all")
@Transactional
public String extendAll(Authentication auth) {
    var user = userRepository.findByName(auth.getName()).orElse(null);
    if (user == null) return "redirect:/?error=unauth";
    Long userId = user.getId();

    var loans = loanRepository.findByUserIdAndReturnedAtIsNull(userId);
    int count = 0;
    for (var loan : loans) {
        if (canExtend(loan)) {
            loan.extendByDays(14);
            loanRepository.save(loan);
            count++;
        }
    }
    return "redirect:/my/books?extendedAll=" + count;
}


    private boolean canExtend(Loan loan) {
        if (loan.getReturnedAt() != null)
            return false;
        int r = loan.getRenewals() == null ? 0 : loan.getRenewals();
        if (r >= MAX_RENEWALS)
            return false;
        // NEW RULE: deny extend if any active hold exists on this book
        long queue = waitlistRepository.countByBookIdAndActiveTrue(loan.getBookId());
        return queue == 0;
    }

    private LocalDate nextDue(LocalDate current) {
        LocalDate base = (current == null) ? LocalDate.now() : current;
        return base.plusDays(EXTEND_DAYS);
    }
}
