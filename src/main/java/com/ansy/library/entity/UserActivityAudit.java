package com.ansy.library.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_activity_audit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActivityAudit {

    @Id
    private UUID id = UUID.randomUUID();

    private UUID userId;

    private String email;

    private String activityType; // REGISTER, LOGIN, LOGOUT, etc

    private boolean success;

    private String ipAddress;

    private String userAgent;

    private Instant activityTime = Instant.now();
}
