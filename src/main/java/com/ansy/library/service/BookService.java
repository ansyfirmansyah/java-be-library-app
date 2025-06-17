package com.ansy.library.service;

import com.ansy.library.dto.BookDto;
import com.ansy.library.entity.Book;
import com.ansy.library.exception.NotFoundException;
import com.ansy.library.repository.BookRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final MessageSource messageSource;

    public List<BookDto> getAvailableBooks() {
        return bookRepository.findByAvailableTrue().stream()
                .map(book -> BookDto.builder()
                        .id(book.getId())
                        .title(book.getTitle())
                        .author(book.getAuthor())
                        .build())
                .collect(Collectors.toList());
    }

    public BookDto getBookById(UUID id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage("book.notFound", null, LocaleContextHolder.getLocale())
                ));
        return BookDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .build();
    }

    public BookDto createBook(@Valid BookDto bookDto) {
        Book book = Book.builder()
                .title(bookDto.getTitle())
                .author(bookDto.getAuthor())
                .available(true)
                .build();
        Book saved = bookRepository.save(book);
        return BookDto.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .author(saved.getAuthor())
                .build();
    }

    public BookDto updateBook(UUID id, @Valid BookDto bookDto) {
        Book existingBook = bookRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException(
                        messageSource.getMessage("book.notFound", null, LocaleContextHolder.getLocale())
                )
        );
        existingBook.setTitle(bookDto.getTitle());
        existingBook.setAuthor(bookDto.getAuthor());
        Book updated = bookRepository.save(existingBook);
        return BookDto.builder()
                .id(updated.getId())
                .title(updated.getTitle())
                .author(updated.getAuthor())
                .build();

    }

    public void deleteBook(UUID id) {
        boolean existingBook = bookRepository.existsById(id);
        if (!existingBook) {
            throw new NotFoundException(
                    messageSource.getMessage("book.notFound", null, LocaleContextHolder.getLocale())
            );
        }
        bookRepository.deleteById(id);
    }
}
