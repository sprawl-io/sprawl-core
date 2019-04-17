package org.thomaschen.sprawl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.thomaschen.sprawl.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    public Optional<User> findByUsername(String username);
}
