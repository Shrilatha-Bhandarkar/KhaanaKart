package com.onlinefoodorder.controller;

import com.onlinefoodorder.dto.AuthRequest;
import com.onlinefoodorder.dto.AuthResponse;
import com.onlinefoodorder.dto.UserRegistration;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.repository.UserRepository;
import com.onlinefoodorder.security.JwtUtil;
import com.onlinefoodorder.service.AuthService;
import com.onlinefoodorder.util.Status.ApprovalStatus;
import com.onlinefoodorder.exception.UserNotFoundException;
import com.onlinefoodorder.exception.UnauthorizedAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

/**
 * Controller for handling authentication-related operations such as login and
 * registration.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AuthService authService;
	private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

	/**
	 * Handles user login and JWT token generation.
	 * 
	 * @param authRequest the authentication request containing email and password.
	 * @return JWT token if authentication is successful.
	 */
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
		try {
			logger.info("Attempting login for user: {}", authRequest.getEmail());
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));

			User user = userRepository.findByEmail(authRequest.getEmail()).orElseThrow(() -> {
				logger.error("User not found: {}", authRequest.getEmail());
				return new UserNotFoundException("User not found");
			});

			if (user.getApprovalStatus() != ApprovalStatus.APPROVED) {
				logger.error("User {} is not approved. Access denied.", authRequest.getEmail());
				throw new UnauthorizedAccessException("Account not approved. Contact admin.");
			}

			String token = jwtUtil.generateToken(user.getEmail());
			logger.info("Login successful for user: {}", authRequest.getEmail());
			return ResponseEntity.ok(new AuthResponse(token));

		} catch (BadCredentialsException e) {
			logger.error("Bad credentials for user {}: {}", authRequest.getEmail(), e.getMessage());
			throw new UnauthorizedAccessException("Invalid credentials");
		} catch (UserNotFoundException | UnauthorizedAccessException e) {
			// Don't wrap these specific exceptions
			throw e;
		} catch (Exception e) {
			logger.error("Unexpected error during login for user {}: {}", authRequest.getEmail(), e.getMessage());
			throw new RuntimeException("Error during login", e);
		}
	}

	/**
	 * Handles user registration.
	 * 
	 * @param dto the user registration request.
	 * @return success message.
	 */
	@PostMapping("/register")
	public ResponseEntity<String> registerUser(@Valid @RequestBody UserRegistration dto) {
		try {
			logger.info("Registering user with email {}", dto.getEmail());
			String response = authService.registerUser(dto);
			logger.info("Registration successful for user {}", dto.getEmail());
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			logger.error("Error during registration for user {}: {}", dto.getEmail(), e.getMessage());
			return ResponseEntity.status(500).body("Registration failed. Please try again.");
		}
	}

	/**
	 * Handles user logout by clearing JWT token from the client-side.
	 * 
	 * @return success message
	 */
	@PostMapping("/logout")
	public ResponseEntity<String> logout() {
		// No backend action needed for JWT. It's a client-side action.
		return ResponseEntity.ok("Logout successful");
	}
}