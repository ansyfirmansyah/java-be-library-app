package com.ansy.library.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

/*
Untuk class @Entity seperti User, tidak direkomendasikan pakai @Data karena:
@EqualsAndHashCode dan @ToString bisa sebabkan loop rekursif atau query tambahan
jika ada relasi @OneToMany, @ManyToOne, dll.
Lebih baik pakai hanya @Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor, @Builder
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id = UUID.randomUUID();

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
    // Menggunakan object java.time.Instant karena
    // Representasi timestamp UTC (1970-01-01T00:00:00Z epoch)
    // Cocok untuk audit waktu global
    // Aman timezone (dibanding java.util.Date, LocalDateTime)

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PrePersist // dipanggil sebelum insert baru
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate // dipanggil sebelum update record
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
