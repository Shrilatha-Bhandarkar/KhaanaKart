package com.onlinefoodorder.service;

import com.onlinefoodorder.dto.PaymentDto;
import com.onlinefoodorder.entity.*;
import com.onlinefoodorder.exception.*;
import com.onlinefoodorder.repository.*;
import com.onlinefoodorder.util.PdfGenerator;
import com.onlinefoodorder.util.Status.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PdfGenerator pdfGenerator;
    
    @InjectMocks
    private PaymentService paymentService;
    
    private Order order;
    private User user;
    private Payment payment;
    private PaymentDto paymentDto;
    
    @BeforeEach
    void setUp() {
        // Setup User
        user = new User();
        user.setUserId(1L);
        user.setEmail("test@example.com");
        user.setRole(UserRole.CUSTOMER);
        
        // Setup Order
        order = new Order();
        order.setOrderId(1L);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        
        // Setup Payment
        payment = new Payment();
        payment.setPaymentId(1L);
        payment.setOrder(order);
        payment.setUser(user);
        payment.setAmount(new BigDecimal("100.00"));
        payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setPaymentTime(LocalDateTime.now());
        
        // Setup PaymentDto
        paymentDto = new PaymentDto();
        paymentDto.setOrderId(1L);
        paymentDto.setAmount(new BigDecimal("100.00"));
        paymentDto.setPaymentMethod(PaymentMethod.CREDIT_CARD);
    }
    
    @Test
    void processPayment_Success() throws Exception {
        // Arrange
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(pdfGenerator.generateInvoice(any(Order.class), any(Payment.class))).thenReturn("/path/to/invoice.pdf");
        
        // Act
        PaymentDto result = paymentService.processPayment(paymentDto);
        
        // Assert
        assertNotNull(result);
        assertEquals(PaymentStatus.SUCCESS, result.getPaymentStatus());
        verify(paymentRepository, times(2)).save(any(Payment.class));
        verify(orderRepository).save(order);
    }
    
    @Test
    void processPayment_OrderNotFound_ThrowsException() {
        // Arrange
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> paymentService.processPayment(paymentDto));
    }
    
    @Test
    void processPayment_Failure_ThrowsException() throws Exception {
        // Arrange
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(pdfGenerator.generateInvoice(any(Order.class), any(Payment.class)))
            .thenThrow(new RuntimeException("PDF generation failed"));
        
        // Act & Assert
        assertThrows(RuntimeException.class, 
            () -> paymentService.processPayment(paymentDto));
        
        // Verify failure status was saved
        verify(paymentRepository, times(2)).save(any(Payment.class));
    }
    
    @Test
    void updatePayment_Success() {
        // Arrange
        PaymentDto updateDto = new PaymentDto();
        updateDto.setPaymentStatus(PaymentStatus.REFUNDED);
        
        when(paymentRepository.findById(anyLong())).thenReturn(Optional.of(payment));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        
        // Act
        PaymentDto result = paymentService.updatePayment(1L, updateDto, "test@example.com");
        
        // Assert
        assertEquals(PaymentStatus.REFUNDED, result.getPaymentStatus());
        verify(paymentRepository).save(payment);
    }
    
    @Test
    void updatePayment_UnauthorizedDeliveryPerson_ThrowsException() {
        // Arrange
        User deliveryUser = new User();
        deliveryUser.setUserId(2L);
        deliveryUser.setEmail("delivery@example.com");
        deliveryUser.setRole(UserRole.DELIVERY_PERSON);
        
        // Create a new payment with COD to test the negative case
        Payment codPayment = new Payment();
        codPayment.setPaymentId(2L);
        codPayment.setOrder(order);
        codPayment.setUser(user);
        codPayment.setPaymentMethod(PaymentMethod.CREDIT_CARD); // Not COD
        codPayment.setPaymentStatus(PaymentStatus.SUCCESS);
        
        when(paymentRepository.findById(anyLong())).thenReturn(Optional.of(codPayment));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(deliveryUser));
        
        PaymentDto updateDto = new PaymentDto();
        updateDto.setPaymentStatus(PaymentStatus.REFUNDED);
        
        // Act & Assert
        assertThrows(UnauthorizedAccessException.class,
            () -> paymentService.updatePayment(2L, updateDto, "delivery@example.com"));
    }
    
    @Test
    void updatePayment_PaymentNotFound_ThrowsException() {
        // Arrange
        when(paymentRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
            () -> paymentService.updatePayment(1L, new PaymentDto(), "test@example.com"));
    }
    
    @Test
    void updatePayment_UserNotFound_ThrowsException() {
        // Arrange
        when(paymentRepository.findById(anyLong())).thenReturn(Optional.of(payment));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
            () -> paymentService.updatePayment(1L, new PaymentDto(), "test@example.com"));
    }
    
    @Test
    void getPaymentById_Success() {
        // Arrange
        when(paymentRepository.findById(anyLong())).thenReturn(Optional.of(payment));
        
        // Act
        PaymentDto result = paymentService.getPaymentById(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getPaymentId());
    }
    
    @Test
    void getPaymentById_NotFound_ThrowsException() {
        // Arrange
        when(paymentRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(PaymentFailedException.class,
            () -> paymentService.getPaymentById(1L));
    }
}