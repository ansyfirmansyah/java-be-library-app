package com.ansy.library.controller;

import com.ansy.library.dto.BookDto;
import com.ansy.library.entity.Book;
import com.ansy.library.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional // agar data test dihapus setelah selesai testing
public class BookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void clearDatabase() {
        bookRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN") // ditambahkan role admin karena endpoint ini harus role admin
    void whenCreateBook_thenReturn201AndLocationHeader() throws Exception {
        BookDto dto = BookDto.builder()
                .title("Negeri Tujuh Menara")
                .author("Sultan Al Fatih")
                .build();
        mockMvc.perform(
                        post("/books")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.message").value("Sukses menyimpan buku"))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.title").value(dto.getTitle()))
                .andExpect(jsonPath("$.data.author").value(dto.getAuthor()));

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenUpdateBook_thenReturn200AndUpdateEntity() throws Exception {
        // 1. Buat data awal
        Book saved = bookRepository.save(
                Book.builder()
                        .title("Book 1")
                        .author("Test")
                        .available(true)
                        .build()
        );

        UUID id = saved.getId();

        // 2. Siapkan data DTO
        BookDto bookDto = BookDto.builder()
                .title("Book 1 Update")
                .author("Test Update")
                .build();

        // 3. Test dengan Mock MVC
        mockMvc.perform(
                        put("/books/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(bookDto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Sukses memperbarui buku"))
                .andExpect(jsonPath("$.data.id").value(id.toString()))
                .andExpect(jsonPath("$.data.title").value(bookDto.getTitle()))
                .andExpect(jsonPath("$.data.author").value(bookDto.getAuthor()));

        // 4. Verify Persistence
        Book updated = bookRepository.findById(id).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo(bookDto.getTitle());
        assertThat(updated.getAuthor()).isEqualTo(bookDto.getAuthor());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenDeleteBook_thenReturn204() throws Exception {
        // 1. Inisiasi data
        Book saved = bookRepository.save(
                Book.builder()
                        .title("Tes hapus buku")
                        .author("Tes")
                        .available(true)
                        .build()
        );
        UUID id = saved.getId();

        // 2. Test dengan Mock MVC
        mockMvc.perform(delete("/books/{id}", id))
                .andExpect(status().isNoContent());

        // 3. Verifikasi di database
        assertThat(bookRepository.existsById(id)).isFalse();
    }
}
