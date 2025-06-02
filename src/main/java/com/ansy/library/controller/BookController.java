package com.ansy.library.controller;

import com.ansy.library.dto.ApiResponse;
import com.ansy.library.dto.BookDto;
import com.ansy.library.service.BookService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
