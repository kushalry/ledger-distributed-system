package com.kushal.ledger.repository;

import com.kushal.ledger.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    // Empty! Spring Data JPA automatically provides methods like:
    // save(), findById(), findAll(), delete()
}
