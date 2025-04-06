package com.onlinefoodorder.service;

import com.onlinefoodorder.dto.UserRegistration;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.exception.ResourceNotFoundException;
import com.onlinefoodorder.repository.UserRepository;
import com.onlinefoodorder.util.Status.ApprovalStatus;
import com.onlinefoodorder.util.Status.UserRole;
import com.onlinefoodorder.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for handling authentication logic such as user registration.
 */

@Service
public class AuthService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

	/**
	 * Registers a new user in the system. Ensures unique username and assigns
	 * appropriate approval status based on role.
	 *
	 * @param userRegistration DTO containing user registration details
	 * @return Success message based on approval status
	 */
	@Transactional
	public String registerUser(UserRegistration userRegistration) {
		try {
			logger.info("Checking if username {} is already taken", userRegistration.getUsername());
			if (userRepository.existsByUsername(userRegistration.getUsername())) {
				logger.error("Username {} is already taken", userRegistration.getUsername());
				throw new ResourceNotFoundException("Username is already taken");
			}

			// Creating a new user entity
			User user = new User();
			user.setEmail(userRegistration.getEmail());
			user.setUsername(userRegistration.getUsername());
			user.setPassword(passwordEncoder.encode(userRegistration.getPassword()));
			user.setRole(UserRole.valueOf(userRegistration.getRole().toUpperCase()));
			user.setFirstName(userRegistration.getFirstName());
			user.setLastName(userRegistration.getLastName());
			user.setPhone(userRegistration.getPhone());
			user.setActive(true);

			// Approval logic based on role
			if (UserRole.CUSTOMER.name().equalsIgnoreCase(userRegistration.getRole())) {
				user.setApprovalStatus(ApprovalStatus.APPROVED);
				logger.info("User {} registered successfully with APPROVED status", userRegistration.getUsername());
			} else {
				user.setApprovalStatus(ApprovalStatus.PENDING);
				logger.info("User {} registered with PENDING approval status", userRegistration.getUsername());
			}

			userRepository.save(user);
			return user.getApprovalStatus() == ApprovalStatus.APPROVED ? "User registered successfully!"
					: "Registration successful. Waiting for admin approval.";
		} catch (ResourceNotFoundException | IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error registering user {}: {}", userRegistration.getUsername(), e.getMessage());
			throw new RuntimeException("Error registering user", e);
		}
	}
}