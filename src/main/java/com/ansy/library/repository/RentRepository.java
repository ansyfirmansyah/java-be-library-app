package com.ansy.library.repository;

import com.ansy.library.entity.Rent;
import com.ansy.library.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface RentRepository extends JpaRepository<Rent, UUID> {
    Optional<Rent> findByUserAndReturnedAtIsNull(User user);

    Collection<Rent> findByReturnedAtIsNull();

    Collection<Rent> findByDueAtBeforeAndReturnedAtIsNull(Instant now);
}
