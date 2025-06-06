package com.ansy.library.task;

import com.ansy.library.repository.PasswordResetTokenRepository;
import com.ansy.library.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Scheduler ini tidak aman jika service di-deploy di multi-pod
 * Semua pod akan jalankan task yang sama
 * Akibatnya: cleanup bisa dijalankan berkali-kali dalam waktu yang sama
 * Solusinya: Gunakan distributed lock -> Redis + @SchedulerLock (pakai lib ShedLock)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CleanupTask {

    private final PasswordResetTokenRepository tokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 * * * *") // Setiap jam
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        int deleted = tokenRepository.deleteByExpiresAtBeforeOrUsedIsTrue(now);
        log.info("🧹 Password reset tokens dibersihkan: {} records", deleted);
    }

    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void cleanExpiredRefreshTokens() {
        int delete = refreshTokenRepository.deleteAllExpiredSince(Instant.now());
        log.info("🧹 Running scheduled cleanup of expired refresh tokens: {} records", delete);
    }
}
