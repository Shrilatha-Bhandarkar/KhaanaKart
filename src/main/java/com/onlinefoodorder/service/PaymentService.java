package com.onlinefoodorder.service;

import com.onlinefoodorder.dto.PaymentDto;
import com.onlinefoodorder.entity.*;
import com.onlinefoodorder.repository.*;
import com.onlinefoodorder.util.PdfGenerator;
import com.onlinefoodorder.util.Status.OrderStatus;
import com.onlinefoodorder.util.Status.PaymentMethod;
import com.onlinefoodorder.util.Status.PaymentStatus;
import com.onlinefoodorder.util.Status.UserRole;
import com.onlinefoodorder.exception.PaymentFailedException;
import com.onlinefoodorder.exception.ResourceNotFoundException;
import com.onlinefoodorder.exception.UnauthorizedAccessException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service class for handling payment operations.
 */
@Service
public class PaymentService {

	private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

	@Autowired
	private PaymentRepository paymentRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PdfGenerator pdfGenerator;

	/**
	 * Processes a new payment transaction.
	 * 
	 * @param paymentDto DTO containing payment details.
	 * @return DTO representing the processed payment.
	 */
	public PaymentDto processPayment(PaymentDto paymentDto) {
		// Fetch Order
		Order order = orderRepository.findById(paymentDto.getOrderId()).orElseThrow(
				() -> new ResourceNotFoundException("Order not found with ID: " + paymentDto.getOrderId()));

		// Fetch User
		User user = order.getUser(); // User is already linked to Order

		logger.info("Processing payment for Order ID: {}", order.getOrderId());

		Payment payment = new Payment();
		try {
			// Create Payment Object
			payment.setOrder(order);
			payment.setUser(user);
			payment.setAmount(paymentDto.getAmount());
			payment.setPaymentMethod(paymentDto.getPaymentMethod());
			payment.setTransactionId(UUID.randomUUID().toString()); // Unique transaction ID
			payment.setPaymentTime(LocalDateTime.now());

			// Automatically Set Payment Status
			payment.setPaymentStatus(PaymentStatus.SUCCESS);

			// Save Payment
			Payment savedPayment = paymentRepository.save(payment);
			order.setPayment(savedPayment);
			orderRepository.save(order);

			// Generate PDF Invoice & Update Payment
			String invoicePath = pdfGenerator.generateInvoice(order, savedPayment);
			savedPayment.setInvoiceUrl(invoicePath);
			paymentRepository.save(savedPayment);

			logger.info("Payment successful. Transaction ID: {}", savedPayment.getTransactionId());
			return mapToDto(savedPayment);
		} catch (Exception e) {
			// Handle Payment Failure
			logger.error("Payment failed for Order ID: {}. Reason: {}", order.getOrderId(), e.getMessage());

			payment.setPaymentStatus(PaymentStatus.FAILED);
			paymentRepository.save(payment); // Save failure status

			throw new RuntimeException("Payment failed: " + e.getMessage()); // Inform frontend
		}
	}

	/**
	 * Updates an existing payment's status or details.
	 * 
	 * @param paymentId ID of the payment to update.
	 * @param dto       DTO containing updated payment details.
	 * @return DTO representing the updated payment.
	 */
	public PaymentDto updatePayment(long paymentId, PaymentDto dto, String userEmail) {
		Payment payment = paymentRepository.findById(paymentId)
				.orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

		// Delivery personnel restrictions
		if (user.getRole().equals(UserRole.DELIVERY_PERSON)) {
			if (payment.getPaymentMethod() != PaymentMethod.CASH_ON_DELIVERY) {
				throw new UnauthorizedAccessException("Delivery personnel can only update COD payments.");
			}
			// Delivery can only update status
			if (dto.getPaymentStatus() == null || dto.getAmount() != null || dto.getPaymentMethod() != null
					|| dto.getTransactionId() != null) {
				throw new UnauthorizedAccessException("Delivery personnel can only update payment status.");
			}
		}

		// Apply updates
		if (dto.getPaymentStatus() != null) {
			payment.setPaymentStatus(dto.getPaymentStatus());
		}

		// Only allow these fields to be updated by admins
		if (!user.getRole().equals(UserRole.DELIVERY_PERSON)) {
			if (dto.getAmount() != null) {
				payment.setAmount(dto.getAmount());
			}
			if (dto.getPaymentMethod() != null) {
				payment.setPaymentMethod(dto.getPaymentMethod());
			}
			if (dto.getTransactionId() != null) {
				payment.setTransactionId(dto.getTransactionId());
			}
		}

		Payment updatedPayment = paymentRepository.save(payment);
		logger.info("Payment ID {} updated successfully by {}", paymentId, userEmail);
		return mapToDto(updatedPayment);
	}

	/**
	 * Retrieves an invoice file path for a given payment ID.
	 */
	/**
	 * Retrieves a payment by its ID.
	 * 
	 * @param paymentId ID of the requested payment.
	 * @return DTO representing the retrieved payment.
	 */
	public PaymentDto getPaymentById(Long paymentId) {
		Payment payment = paymentRepository.findById(paymentId)
				.orElseThrow(() -> new PaymentFailedException("Payment not found with ID: " + paymentId));
		return mapToDto(payment);
	}

	/**
	 * Maps a Payment entity to a PaymentDto.
	 * 
	 * @param payment The payment entity to map.
	 * @return DTO containing payment details.
	 */
	private PaymentDto mapToDto(Payment payment) {
		return new PaymentDto(payment.getPaymentId(), payment.getOrder().getOrderId(), payment.getUser().getUserId(),
				payment.getAmount(), payment.getPaymentStatus(), payment.getPaymentMethod(), payment.getTransactionId(),
				payment.getInvoiceUrl(), payment.getPaymentTime());
	}
}
