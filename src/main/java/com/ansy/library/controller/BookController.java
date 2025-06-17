package com.ansy.library.controller;

import com.ansy.library.dto.ApiResponse;
import com.ansy.library.dto.BookDto;
import com.ansy.library.entity.Book;
import com.ansy.library.service.BookService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Tag(name = "Book")
public class BookController {

    private final BookService bookService;
    private final MessageSource messageSource;

    @GetMapping
    public ResponseEntity<ApiResponse> getAvailableBooks() {
        List<BookDto> books = bookService.getAvailableBooks();
        return ResponseEntity.ok(ApiResponse.success(
                messageSource.getMessage("book.list.success", null, LocaleContextHolder.getLocale()),
                books
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getBookById(@PathVariable UUID id) {
        BookDto book = bookService.getBookById(id);
        return ResponseEntity.ok(ApiResponse.success(
                messageSource.getMessage("book.one.success", null, LocaleContextHolder.getLocale()),
                book
        ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse> createBook(@RequestBody @Valid BookDto bookDto) {
        BookDto created = bookService.createBook(bookDto);
        String message = messageSource.getMessage("book.create.success", null, LocaleContextHolder.getLocale());
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(
                uri
        ).body(
                ApiResponse.success(message, created)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateBook(@PathVariable UUID id, @RequestBody @Valid BookDto bookDto) {
        BookDto updated = bookService.updateBook(id, bookDto);
        String message = messageSource.getMessage("book.update.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(message, updated));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteBook(@PathVariable UUID id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
