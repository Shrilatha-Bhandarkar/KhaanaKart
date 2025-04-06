package com.onlinefoodorder.security;

import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.service.UserService;
import com.onlinefoodorder.util.Status.ApprovalStatus;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * JWT Authentication Filter for validating and processing JWT tokens in
 * requests.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
	private final JwtUtil jwtUtil;
	private final UserService userService;

	public JwtAuthenticationFilter(JwtUtil jwtUtil, UserService userService) {
		this.jwtUtil = jwtUtil;
		this.userService = userService;
	}

	/**
	 * Intercepts and validates JWT tokens for authentication.
	 *
	 * @param request  HTTP request containing the JWT token.
	 * @param response HTTP response for sending authentication errors.
	 * @param chain    The filter chain.
	 * @throws ServletException in case of filter errors.
	 * @throws IOException      in case of I/O errors.
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		final String authHeader = request.getHeader("Authorization");
		final String requestURI = request.getRequestURI();

		if (requestURI.startsWith("/auth/register") || requestURI.startsWith("/auth/login")) {
			chain.doFilter(request, response);
			return;
		}

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			logger.warn("Missing or invalid Authorization header");
			chain.doFilter(request, response);
			return;
		}

		String jwt = authHeader.substring(7);
		String userEmail;

		try {
			userEmail = jwtUtil.extractEmail(jwt);
			logger.info("Extracted email from JWT: {}", userEmail);
		} catch (ExpiredJwtException e) {
			logger.error("JWT token expired: {}", e.getMessage());
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Expired Token");
			return;
		} catch (MalformedJwtException | SignatureException e) {
			logger.error("Invalid JWT token: {}", e.getMessage());
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token");
			return;
		}

		if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UserDetails userDetails = userService.loadUserByUsername(userEmail);
			User user = userService.getUserByEmail(userEmail);

			if (user == null) {
				logger.warn("User not found: {}", userEmail);
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
				return;
			}

			if (!ApprovalStatus.APPROVED.equals(user.getApprovalStatus())) {
				logger.warn("Unauthorized access attempt - user {} approval pending", userEmail);
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Your account is pending admin approval.");
				return;
			}

			if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
				UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
						null, userDetails.getAuthorities());
				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authToken);
				logger.info("User {} authenticated successfully", userEmail);
			} else {
				logger.warn("JWT validation failed for user {}", userEmail);
			}
		}

		chain.doFilter(request, response);
	}
}
