package com.ansy.library.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue //Strategy default, biasanya sama kayak AUTO
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id = UUID.randomUUID(); //Langsung generate UUID di sisi Java, saat new object di Java

    @ManyToOne(fetch = FetchType.LAZY) // Field ini tidak langsung di-load dari database saat entity diambil.
    // Hanya di-load kalau benar-benar dipanggil (akses getUser()), baru query ke DB untuk ambil user-nya.
    // Keuntungan: Lebih hemat performa/memori (lazy loading).
    // Downside: Kalau akses di luar transaction/session, bisa error (LazyInitializationException).
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean used;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
