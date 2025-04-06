package com.onlinefoodorder.controller;

import com.onlinefoodorder.dto.PaymentDto;
import com.onlinefoodorder.service.PaymentService;
import com.onlinefoodorder.exception.ResourceNotFoundException;

import java.io.File;
import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for handling payment-related operations.
 */
@RestController
@RequestMapping("/payments")
public class PaymentController {

	private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

	@Autowired
	private PaymentService paymentService;

	/**
	 * Processes a new payment.
	 * 
	 * @param paymentDto DTO containing payment details.
	 * @return ResponseEntity containing the saved payment details.
	 */
	@PostMapping("/process")
	public ResponseEntity<PaymentDto> processPayment(@RequestBody PaymentDto paymentDto) {
		logger.info("Processing payment for order ID: {}", paymentDto.getOrderId());
		PaymentDto savedPayment = paymentService.processPayment(paymentDto);
		logger.info("Payment processed successfully with transaction ID: {}. Invoice URL: {}",
				savedPayment.getTransactionId(), savedPayment.getInvoiceUrl());
		return ResponseEntity.ok(savedPayment);
	}

	/**
	 * Updates payment status.
	 * 
	 * @param paymentId ID of the payment to be updated.
	 * @param dto       DTO containing updated payment information.
	 * @return ResponseEntity with updated payment details.
	 */
	@PutMapping("/status/{paymentId}")
	public ResponseEntity<PaymentDto> updatePayment(@PathVariable Long paymentId, @RequestBody PaymentDto dto,
			Principal principal) {
		String userEmail = principal.getName(); // Get logged-in user email
		logger.info("Updating payment status for payment ID: {} by {}", paymentId, userEmail);
		return ResponseEntity.ok(paymentService.updatePayment(paymentId, dto, userEmail));
	}

	/**
	 * Retrieves a payment by ID.
	 * 
	 * @param paymentId ID of the payment to retrieve.
	 * @return ResponseEntity with the requested payment details.
	 */
	@GetMapping("/{paymentId}")
	public ResponseEntity<PaymentDto> getPaymentById(@PathVariable Long paymentId) {
		logger.info("Fetching payment details for ID: {}", paymentId);
		PaymentDto paymentDto = paymentService.getPaymentById(paymentId);
		return ResponseEntity.ok(paymentDto);
	}
}