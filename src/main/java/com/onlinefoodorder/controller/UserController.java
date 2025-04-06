package com.onlinefoodorder.controller;

import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.exception.UserNotFoundException;
import com.onlinefoodorder.service.UserService;
import com.onlinefoodorder.util.Status.UserRole;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * Controller for handling user-related operations.
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserService userService;

	/**
	 * Retrieves the profile of the currently authenticated user.
	 *
	 * @param principal The authenticated user's details.
	 * @return ResponseEntity containing user details.
	 */
	@GetMapping("/profile")
    public ResponseEntity<User> getUserProfile(Principal principal) {
        logger.info("Fetching profile for user: {}", principal.getName());

        User user = userService.getUserByEmail(principal.getName());
        if (user == null) {
            logger.error("User not found: {}", principal.getName());
            throw new UserNotFoundException("User not found with email: " + principal.getName());
        }

        // Build profile path safely
        String rolePath = buildProfilePath(user.getRole());

        logger.info("Profile retrieved successfully for user: {}", principal.getName());
        return ResponseEntity.ok()
                .header("Profile-Path", rolePath)
                .body(user);
    }

    private String buildProfilePath(UserRole role) {
        String basePath = (role != null) ? 
            switch (role) {
                case ADMIN -> "admin";
                case RESTAURANT_OWNER -> "restaurant";
                case CUSTOMER -> "customer";
                case DELIVERY_PERSON -> "delivery";
            } : "user";
        
        return "/" + basePath + "/profile";
    }
}