package com.onlinefoodorder.config;

import com.onlinefoodorder.security.JwtAuthenticationFilter;
import com.onlinefoodorder.service.UserService;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
	private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserService userService;
    
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, UserService userService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userService = userService;
    }

    /**
     * Configures security settings including authentication, authorization, and JWT filtering.
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    	logger.info("Configuring security filter chain...");
    	http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
            		
            	    .requestMatchers("/auth/register", "/auth/login","/auth/logout").permitAll()
            	    .requestMatchers("/restaurant/menu-category/**", "/restaurant/menu-item/**").permitAll()
            	    .requestMatchers(HttpMethod.GET, "/restaurant/{id}").permitAll()
            	    .requestMatchers(HttpMethod.GET, "/restaurant/all").permitAll()
            	    .requestMatchers(HttpMethod.GET, "/reviews/restaurant/**").permitAll()
            	    .requestMatchers(HttpMethod.GET, "/restaurant/menu-category/{restaurantId}/{categoryId}").permitAll()
            	    .requestMatchers(HttpMethod.GET, "/restaurant/menu-category/{restaurantId}/all").permitAll()
            	    .requestMatchers(HttpMethod.GET, "/restaurant/menu-item/{restaurantId}/{categoryId}/all").permitAll()
            	    .requestMatchers(HttpMethod.GET, "/coupons").permitAll()
            	    .requestMatchers(HttpMethod.GET, "/coupons/{code}").permitAll()
            	    
            	    //Customer Access 
            	    .requestMatchers("/customer/orders/place", "/customer/orders/user", "/customer/cart/**", "/customer/address/**").hasRole("CUSTOMER")
            	    .requestMatchers(HttpMethod.POST, "/payments/process").hasRole("CUSTOMER")  // Customer makes a payment
            	    .requestMatchers(HttpMethod.GET, "/payments/{paymentId}").hasRole("CUSTOMER")  // Customer views payment details
            	    .requestMatchers("/customer/reviews/**").hasRole("CUSTOMER")
            	    .requestMatchers(HttpMethod.POST, "/orders/coupons/apply").hasRole("CUSTOMER") // Apply Coupon
            	     .requestMatchers(HttpMethod.DELETE,"/coupons/remove/{orderId}").hasRole("CUSTOMER")
            	    
            	     // Restaurant Owner Access 
            	    .requestMatchers(HttpMethod.PUT, "/orders/status/{orderId}").hasRole("RESTAURANT_OWNER")
            	    .requestMatchers(HttpMethod.PUT, "/payments/status/**").hasAnyRole("RESTAURANT_OWNER", "DELIVERY")
            	    .requestMatchers(HttpMethod.POST, "/restaurant/menu-category", "/restaurant/menu-item").hasRole("RESTAURANT_OWNER")
            	    .requestMatchers(HttpMethod.PUT, "/restaurant/menu-category/{categoryId}", "/restaurant/menu-item/{itemId}").hasRole("RESTAURANT_OWNER")
            	    .requestMatchers(HttpMethod.DELETE, "/restaurant/menu-category/{categoryId}", "/restaurant/menu-item/{itemId}", "/restaurant/{id}").hasRole("RESTAURANT_OWNER")
            	    .requestMatchers(HttpMethod.POST, "/coupons/restaurant/{restaurantId}").hasRole("RESTAURANT_OWNER") 
            	    .requestMatchers(HttpMethod.PUT, "/coupons/restaurant/{restaurantId}/{id}").hasRole("RESTAURANT_OWNER")
            	    .requestMatchers(HttpMethod.DELETE, "/coupons/restaurant/{restaurantId}/{id}").hasRole("RESTAURANT_OWNER")

            	    // Delivery Person Access 
            	    .requestMatchers(HttpMethod.PUT, "/delivery/orders/{orderId}/status").hasRole("DELIVERY")
            	    .requestMatchers("/delivery/orders/pending").hasRole("DELIVERY")
            	    .requestMatchers("/delivery/orders/deliveries").hasRole("DELIVERY")

            	    // Admin Access
            	    .requestMatchers("/admin/**").hasRole("ADMIN")
            	    .requestMatchers(HttpMethod.POST, "/coupons/global").hasRole("ADMIN")
            	    .requestMatchers(HttpMethod.PUT, "/coupons/global/{id}").hasRole("ADMIN")
            	    .requestMatchers(HttpMethod.DELETE, "/coupons/global/{id}").hasRole("ADMIN")

            	    .anyRequest().authenticated()
            	)
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) ->{
                	 logger.warn("Unauthorized access attempt: {}", authException.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                })
             )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configures authentication manager with user details service and password encoder.
     */
    
    @Bean
    AuthenticationManager authenticationManager() {
    	logger.info("Initializing authentication manager...");
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authProvider);
    }


    /**
     * Provides password encoder for hashing passwords securely.
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
