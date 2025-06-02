package com.ansy.library.service;

import com.ansy.library.dto.AdminRentViewResponse;
import com.ansy.library.dto.RentRequest;
import com.ansy.library.dto.RentResponse;
import com.ansy.library.entity.Book;
import com.ansy.library.entity.Rent;
import com.ansy.library.entity.User;
import com.ansy.library.repository.BookRepository;
import com.ansy.library.repository.RentRepository;
import com.ansy.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentService {

    private final RentRepository rentRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    @Transactional
    public RentResponse rentBook(UUID userId, RentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        messageSource.getMessage("user.notFound", null, LocaleContextHolder.getLocale())
                ));

        if (rentRepository.findByUserAndReturnedAtIsNull(user).isPresent()) {
            String message = messageSource.getMessage("rent.alreadyExists", null, LocaleContextHolder.getLocale());
            throw new IllegalStateException(message);
        }

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new IllegalArgumentException(
                        messageSource.getMessage("book.notFound", null, LocaleContextHolder.getLocale())
                ));

        if (!book.isAvailable()) {
            throw new IllegalStateException(
                    messageSource.getMessage("book.notAvailable", null, LocaleContextHolder.getLocale())
            );
        }

        book.setAvailable(false);
        bookRepository.save(book);

        Rent rent = Rent.builder()
                .user(user)
                .book(book)
                .rentedAt(Instant.now())
                .dueAt(Instant.now().plus(request.getDays(), ChronoUnit.DAYS))
                .build();

        Rent saved = rentRepository.save(rent);

        return RentResponse.builder()
                .rentId(saved.getId())
                .bookId(book.getId())
                .bookTitle(book.getTitle())
                .rentedAt(saved.getRentedAt())
                .dueAt(saved.getDueAt())
                .build();
    }

    @Transactional
    public void returnBook(String userId, Instant returnedInstant) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new IllegalArgumentException(
                        messageSource.getMessage("user.notFound", null, LocaleContextHolder.getLocale())
                ));

        Rent rent = rentRepository.findByUserAndReturnedAtIsNull(user)
                .orElseThrow(() -> new IllegalStateException(
                        messageSource.getMessage("rent.noData", null, LocaleContextHolder.getLocale())
                ));

        rent.setReturnedAt(returnedInstant);
        rent.getBook().setAvailable(true);

        rentRepository.save(rent);
        bookRepository.save(rent.getBook());
    }

    @Transactional(readOnly = true)
    public List<AdminRentViewResponse> getAllUnreturnedBooks() {
        return rentRepository.findByReturnedAtIsNull().stream()
                .map(this::toAdminResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdminRentViewResponse> getOverdueBooks() {
        return rentRepository.findByDueAtBeforeAndReturnedAtIsNull(Instant.now()).stream()
                .map(this::toAdminResponse)
                .collect(Collectors.toList());
    }

    private AdminRentViewResponse toAdminResponse(Rent rent) {
        return AdminRentViewResponse.builder()
                .rentId(rent.getId())
                .userId(rent.getUser().getId())
                .userEmail(rent.getUser().getEmail())
                .bookId(rent.getBook().getId())
                .bookTitle(rent.getBook().getTitle())
                .rentedAt(toLocalDate(rent.getRentedAt()))
                .dueAt(toLocalDate(rent.getDueAt()))
                .build();
    }

    private LocalDate toLocalDate(Instant instant) {
        return instant != null ? instant.atZone(ZoneOffset.UTC).toLocalDate() : null;
    }
}
