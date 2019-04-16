package org.thomaschen.sprawl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.thomaschen.sprawl.model.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    public User findUserDetailById(UUID id);
    public User findByUsername(String username);
}
