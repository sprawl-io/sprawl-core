package org.thomaschen.sprawl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.thomaschen.sprawl.model.UserDetail;

import java.util.List;
import java.util.UUID;

public interface UserDetailRepository extends JpaRepository<UserDetail, UUID> {
    public UserDetail findUserDetailById(UUID id);
    public UserDetail findByUsername(String username);
}
