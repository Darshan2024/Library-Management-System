// LibraryManagementSystemApplication.java
package edu.dugale.LibraryManagementSystem;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import edu.dugale.LibraryManagementSystem.data.BookRepository;
import edu.dugale.LibraryManagementSystem.data.UserRepository;
import edu.dugale.LibraryManagementSystem.model.Book;
import edu.dugale.LibraryManagementSystem.model.User;

@SpringBootApplication
public class LibraryManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryManagementSystemApplication.class, args);
    }

    @Bean
    public CommandLineRunner dataLoader(
            UserRepository userRepository,
            BookRepository booksRepository,
            PasswordEncoder encoder) {
        return args -> {

            // Seed users once (immutable username/password)
            if (userRepository.count() == 0) {
                userRepository.save(new User("user1", encoder.encode("password1")));
                userRepository.save(new User("user2", encoder.encode("password2")));
                userRepository.save(new User("user3", encoder.encode("password3")));
                userRepository.save(new User("user4", encoder.encode("password4")));
            }

            // Seed books once
            if (booksRepository.count() == 0) {
                booksRepository.save(new Book(
                        "9780132350884",
                        "Clean Code",
                        "Robert //C. Martin",
                        "A handbook of agile software craftsmanship.",
                        LocalDate.of(2008, 8, 1),
                        true
                ));
                booksRepository.save(new Book(
                        "9780134685991",
                        "Effective Java",
                        "Joshua Bloch",
                        "Best practices for the Java platform.",
                        LocalDate.of(2018, 1, 6),
                        true
                ));
                booksRepository.save(new Book(
                        "9780201633610",
                        "Design Patterns",
                        "Gamma; Helm; Johnson; Vlissides",
                        "Classic patterns for OO design.",
                        LocalDate.of(1994, 10, 31),
                        true
                ));
            }
        };
    }
}
