package com.onlinefoodorder.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.onlinefoodorder.util.Status.*;

/**
 * DTO representing payment details.
 */
public class PaymentDto {
	private Long paymentId;
	@NotNull
	private long orderId;

	@NotNull
	private long userId;

	@NotNull
	@Positive
	private BigDecimal amount;

	@NotNull
	private PaymentStatus paymentStatus;

	@NotNull
	private PaymentMethod paymentMethod;

	private String invoiceUrl;

	public PaymentDto() {
		super();
		// TODO Auto-generated constructor stub
	}

	private String transactionId;
	private LocalDateTime paymentTime;

	public Long getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(Long paymentId) {
		this.paymentId = paymentId;
	}

	public long getOrderId() {
		return orderId;
	}

	public void setOrderId(long orderId) {
		this.orderId = orderId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public PaymentStatus getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public LocalDateTime getPaymentTime() {
		return paymentTime;
	}

	public void setPaymentTime(LocalDateTime paymentTime) {
		this.paymentTime = paymentTime;
	}

	public String getInvoiceUrl() {
		return invoiceUrl;
	}

	public void setInvoiceUrl(String invoiceUrl) {
		this.invoiceUrl = invoiceUrl;
	}

	public PaymentDto(Long paymentId, long orderId, long userId, BigDecimal amount, PaymentStatus paymentStatus,
			PaymentMethod paymentMethod, String invoiceUrl, String transactionId, LocalDateTime paymentTime) {
		super();
		this.paymentId = paymentId;
		this.orderId = orderId;
		this.userId = userId;
		this.amount = amount;
		this.paymentStatus = paymentStatus;
		this.paymentMethod = paymentMethod;
		this.invoiceUrl = invoiceUrl;
		this.transactionId = transactionId;
		this.paymentTime = paymentTime;
	}

}
