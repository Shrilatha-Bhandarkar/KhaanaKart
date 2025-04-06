package com.onlinefoodorder.controller;

import com.onlinefoodorder.dto.PaymentDto;
import com.onlinefoodorder.exception.ResourceNotFoundException;
import com.onlinefoodorder.service.PaymentService;
import com.onlinefoodorder.util.Status.PaymentMethod;
import com.onlinefoodorder.util.Status.PaymentStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.security.Principal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private Principal principal;

    @InjectMocks
    private PaymentController paymentController;

    private PaymentDto testPaymentDto;
    private final String testUserEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        testPaymentDto = new PaymentDto();
        testPaymentDto.setPaymentId(1L);
        testPaymentDto.setOrderId(1001L);
        testPaymentDto.setAmount(new BigDecimal("50.00"));
        testPaymentDto.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        testPaymentDto.setTransactionId("TXN123456");
        testPaymentDto.setPaymentStatus(PaymentStatus.SUCCESS);
    }

    @Test
    void processPayment_ShouldReturnProcessedPayment() {
        // Arrange
        when(paymentService.processPayment(testPaymentDto)).thenReturn(testPaymentDto);

        // Act
        ResponseEntity<PaymentDto> response = paymentController.processPayment(testPaymentDto);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(testPaymentDto, response.getBody());
        verify(paymentService).processPayment(testPaymentDto);
    }

    @Test
    void updatePayment_ShouldReturnUpdatedPayment() {
        // Arrange
        Long paymentId = 1L;
        when(principal.getName()).thenReturn(testUserEmail);
        when(paymentService.updatePayment(paymentId, testPaymentDto, testUserEmail))
            .thenReturn(testPaymentDto);

        // Act
        ResponseEntity<PaymentDto> response = 
            paymentController.updatePayment(paymentId, testPaymentDto, principal);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(testPaymentDto, response.getBody());
        verify(paymentService).updatePayment(paymentId, testPaymentDto, testUserEmail);
    }

    @Test
    void getPaymentById_ShouldReturnPayment() {
        // Arrange
        Long paymentId = 1L;
        when(paymentService.getPaymentById(paymentId)).thenReturn(testPaymentDto);

        // Act
        ResponseEntity<PaymentDto> response = paymentController.getPaymentById(paymentId);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(testPaymentDto, response.getBody());
        verify(paymentService).getPaymentById(paymentId);
    }

    @Test
    void getPaymentById_ShouldHandleNotFound() {
        // Arrange
        Long nonExistentPaymentId = 999L;
        when(paymentService.getPaymentById(nonExistentPaymentId))
            .thenThrow(new ResourceNotFoundException("Payment not found"));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            paymentController.getPaymentById(nonExistentPaymentId));
    }

    @Test
    void updatePayment_ShouldHandleUnauthorizedUser() {
        // Arrange
        Long paymentId = 1L;
        String unauthorizedUser = "unauthorized@example.com";
        when(principal.getName()).thenReturn(unauthorizedUser);
        when(paymentService.updatePayment(paymentId, testPaymentDto, unauthorizedUser))
            .thenThrow(new ResourceNotFoundException("Payment not found or access denied"));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            paymentController.updatePayment(paymentId, testPaymentDto, principal));
    }
}