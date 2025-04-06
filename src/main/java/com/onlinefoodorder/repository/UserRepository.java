package com.onlinefoodorder.repository;

import com.onlinefoodorder.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email.
     * 
     * @param email The email address.
     * @return Optional containing the user if found.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a username already exists.
     * 
     * @param username The username to check.
     * @return True if the username exists, otherwise false.
     */
    boolean existsByUsername(String username);

    /**
     * Finds a user by their username.
     * 
     * @param username The username.
     * @return Optional containing the user if found.
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their ID.
     * 
     * @param id The user ID.
     * @return Optional containing the user if found.
     */
    Optional<User> findById(long id);
}
