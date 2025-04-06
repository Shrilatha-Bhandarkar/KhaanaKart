package com.onlinefoodorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * Entry point for the Online Food Ordering System application.
 * This class bootstraps the Spring Boot application.
 */
@SpringBootApplication
public class OnlineFoodOrderingSystemApplication {
	private static final Logger logger = LoggerFactory.getLogger(OnlineFoodOrderingSystemApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(OnlineFoodOrderingSystemApplication.class, args);
		 logger.info("********KhaanaKart: Online Food Ordering System Application started successfully********");
	}

}
