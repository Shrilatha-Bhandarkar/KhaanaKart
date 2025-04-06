package com.onlinefoodorder.repository;

import com.onlinefoodorder.entity.Order;
import com.onlinefoodorder.entity.Payment;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

	/**
	 * Finds a payment by its transaction ID.
	 * 
	 * @param transactionId The transaction ID.
	 * @return Optional containing the payment if found.
	 */
	Optional<Payment> findByTransactionId(String transactionId);

	/**
	 * Find a payment associated with a specific order ID.
	 * 
	 * @param orderId the ID of the order for which payment details are to be
	 *                retrieved.
	 * @return an Optional containing the Payment if found, or empty if not.
	 */

	Optional<Payment> findByOrderOrderId(Long orderId);

}
