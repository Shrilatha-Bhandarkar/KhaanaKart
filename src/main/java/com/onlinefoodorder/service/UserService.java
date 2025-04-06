package com.onlinefoodorder.service;

import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.exception.UserNotFoundException;
import com.onlinefoodorder.repository.UserRepository;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service for managing user-related operations.
 */
@Service
public class UserService implements UserDetailsService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;


    /**
     * Loads a user by email for authentication purposes.
     *
     * @param email The user's email.
     * @return UserDetails object if found.
     * @throws UsernameNotFoundException If the user is not found.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.info("Loading user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });
    }

    /**
     * Retrieves a user by email.
     *
     * @param email The email of the user.
     * @return The User entity.
     * @throws UserNotFoundException If the user does not exist.
     */
    public User getUserByEmail(String email) {
        logger.info("Fetching user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", email);
                    return new UserNotFoundException("User not found with email: " + email);
                });
    }
    
    public Long getUserIdFromPrincipal(Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Principal cannot be null");
        }
        
        User user = userRepository.findByEmail(principal.getName())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return user.getUserId();
    }

}
