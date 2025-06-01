package com.ansy.library.repository;

import com.ansy.library.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

import static com.ansy.library.repository.UserSql.FIND_BY_EMAIL;

public interface UserRepository extends CrudRepository<User, UUID> {

    @Query(value = FIND_BY_EMAIL, nativeQuery = true)
    Optional<User> findByEmail(@Param("email") String email);

    boolean existsByEmail(String email);

    Optional<Object> findByEmailIgnoreCase(@Email(message = "{login.invalidEmail}") @NotBlank(message = "{login.required.email}") String email);
}

