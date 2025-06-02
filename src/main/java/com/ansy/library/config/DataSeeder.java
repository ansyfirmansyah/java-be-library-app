package com.ansy.library.config;

import com.ansy.library.entity.Book;
import com.ansy.library.entity.Role;
import com.ansy.library.entity.User;
import com.ansy.library.repository.BookRepository;
import com.ansy.library.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class DataSeeder {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @PostConstruct
    public void seedBooks() {
        if (bookRepository.count() == 0) {
            bookRepository.save(Book.builder().title("One Piece").author("Eiichiro Oda").available(true).build());
            bookRepository.save(Book.builder().title("Detective Conan").author("Aoyama Gosho").available(true).build());
            bookRepository.save(Book.builder().title("Dragon Ball").author("Akira Toriyama").available(true).build());
            bookRepository.save(Book.builder().title("Naruto").author("Masashi Kishimoto").available(true).build());
            bookRepository.save(Book.builder().title("Attack on Titan").author("Hajime Isayama").available(true).build());
        }
    }

    @PostConstruct
    public void seedUsers() {
        if (userRepository.count() == 0) {
            userRepository.save(
                    User.builder()
                            .email("dummyadmin@gmail.com")
                            .password("$2a$10$eUIidNd7dWn6CN5XLqg8E.VBCiAfq6a6xfQBKFAqox7KW2NvqSQiS") // Password1
                            .role(Role.ADMIN)
                            .emailVerified(true)
                            .createdAt(Instant.now())
                            .build()
            );
            userRepository.save(
                    User.builder()
                            .email("dummyuser@gmail.com")
                            .password("$2a$10$eUIidNd7dWn6CN5XLqg8E.VBCiAfq6a6xfQBKFAqox7KW2NvqSQiS") // Password1
                            .role(Role.USER)
                            .emailVerified(true)
                            .createdAt(Instant.now())
                            .build()
            );
        }
    }
}
