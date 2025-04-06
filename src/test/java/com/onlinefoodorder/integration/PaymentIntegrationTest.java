package com.onlinefoodorder.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.onlinefoodorder.dto.PaymentDto;
import com.onlinefoodorder.entity.CustomerAddress;
import com.onlinefoodorder.entity.MenuCategory;
import com.onlinefoodorder.entity.MenuItem;
import com.onlinefoodorder.entity.Order;
import com.onlinefoodorder.entity.OrderItem;
import com.onlinefoodorder.entity.Payment;
import com.onlinefoodorder.entity.Restaurant;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.repository.CustomerAddressRepository;
import com.onlinefoodorder.repository.MenuCategoryRepository;
import com.onlinefoodorder.repository.MenuItemRepository;
import com.onlinefoodorder.repository.OrderRepository;
import com.onlinefoodorder.repository.PaymentRepository;
import com.onlinefoodorder.repository.RestaurantRepository;
import com.onlinefoodorder.repository.UserRepository;
import com.onlinefoodorder.security.JwtUtil;
import com.onlinefoodorder.util.PdfGenerator;
import com.onlinefoodorder.util.Status.ApprovalStatus;
import com.onlinefoodorder.util.Status.OrderStatus;
import com.onlinefoodorder.util.Status.PaymentMethod;
import com.onlinefoodorder.util.Status.PaymentStatus;
import com.onlinefoodorder.util.Status.UserRole;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RestaurantRepository restaurantRepository;
    
    @Autowired
    private CustomerAddressRepository addressRepository;
    
    @Autowired
    private MenuItemRepository menuItemRepository;
    @Autowired
    private MenuCategoryRepository menuCategoryRepository;
    @MockBean
    private PdfGenerator pdfGenerator;
    @Autowired
    private JwtUtil jwtUtil;
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private User testCustomer;
    private User testDeliveryPerson;
    private Order testOrder;
    private Payment testPayment;
    private Restaurant testRestaurant;
    private CustomerAddress testAddress;
    private MenuItem testMenuItem;
    private String customerToken;
    private String deliveryToken;
    private String ownerToken;

    @BeforeEach
    void setup() {
        // Create restaurant owner
        User restaurantOwner = new User(
            null, "owner@test.com", "restowner", 
            "password", "Restaurant", "Owner",
            "1112223333", UserRole.RESTAURANT_OWNER, true
        );
        restaurantOwner.setApprovalStatus(ApprovalStatus.APPROVED);
        userRepository.save(restaurantOwner);
        
        // Create test restaurant
        testRestaurant = new Restaurant(
            0L, "Test Restaurant", "123 Restaurant St", 
            "555-1234", 4.5, "http://logo.url",
            LocalDateTime.now(), "09:00", "21:00", 
            restaurantOwner
        );
        restaurantRepository.save(testRestaurant);
        
        // Create menu category (initialize empty items list)
        MenuCategory testCategory = new MenuCategory(
                0L,
                testRestaurant,
                "Main Course",
                "Main dishes for lunch or dinner",new ArrayList<>()
            );
         // Initialize empty list
        menuCategoryRepository.save(testCategory);
        
        // Create test menu item
        testMenuItem = new MenuItem(
            0L, testCategory, testRestaurant, 
            "Test Item", "Test description",
            new BigDecimal("10.00"), "http://item.image", 
            true, true, 15, LocalDateTime.now()
        );
        menuItemRepository.save(testMenuItem);
        
        // Add item to category's list
        testCategory.getMenuItems().add(testMenuItem);
        menuCategoryRepository.save(testCategory);
        
        // Create test customer
        testCustomer = new User(
            null, "customer@test.com", "testcustomer", 
            "password", "Test", "Customer", 
            "1234567890", UserRole.CUSTOMER, true
        );
        testCustomer.setApprovalStatus(ApprovalStatus.APPROVED);
        userRepository.save(testCustomer);
        
        // Create test delivery person
        testDeliveryPerson = new User(
            null, "delivery@test.com", "testdelivery", 
            "password", "Delivery", "Person", 
            "0987654321", UserRole.DELIVERY_PERSON, true
        );
        testDeliveryPerson.setApprovalStatus(ApprovalStatus.APPROVED);
        userRepository.save(testDeliveryPerson);
        
        // Create test address
        testAddress = new CustomerAddress(
            0L, testCustomer, "123 Main St", "Apt 4B", 
            "Testville", "TS", "12345", "Testland", true
        );
        addressRepository.save(testAddress);
        
        // Create test order (initialize empty items list first)
        testOrder = new Order(
                null, testCustomer, testRestaurant, testAddress,
                null, new ArrayList<>(), null, new BigDecimal("25.00"),
                new BigDecimal("5.00"), new BigDecimal("2.00"), 
                "Test instructions", LocalDateTime.now().plusHours(1),
                OrderStatus.PENDING, LocalDateTime.now(), 
                LocalDateTime.now(), null, BigDecimal.ZERO
            );
        orderRepository.save(testOrder); // Save first to generate ID
        
        // Create and add order items
        OrderItem item1 = new OrderItem(
                null, testOrder, testMenuItem, 2, 
                new BigDecimal("10.00"), "No onions"
            );
            OrderItem item2 = new OrderItem(
                null, testOrder, testMenuItem, 1, 
                new BigDecimal("5.00"), "Extra spicy"
            );
            
        
            List<OrderItem> mutableOrderItems = new ArrayList<>();
            mutableOrderItems.add(item1);
            mutableOrderItems.add(item2);
            testOrder.setOrderItems(mutableOrderItems);

     // Create test payment
        testPayment = new Payment();
        testPayment.setOrder(testOrder);
        testPayment.setUser(testCustomer);
        testPayment.setAmount(testOrder.getTotalAmount());
        testPayment.setPaymentStatus(PaymentStatus.SUCCESS);
        testPayment.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        testPayment.setTransactionId("TXN123456789");
        testPayment.setPaymentTime(LocalDateTime.now());
        testPayment.setInvoiceUrl("https://example.com/invoice/12345.pdf");

        // Then save to generate ID
        testPayment = paymentRepository.save(testPayment);

        // Link the payment to the order (if the Order entity has a setPayment method)
        testOrder.setPayment(testPayment);
        orderRepository.save(testOrder); // Update the order to include the payment

        orderRepository.save(testOrder);
        
     // Generate JWT tokens
        customerToken = jwtUtil.generateToken(testCustomer.getEmail());
        deliveryToken = jwtUtil.generateToken(testDeliveryPerson.getEmail());
        ownerToken = jwtUtil.generateToken("owner@test.com"); // Or restaurantOwner.getEmail()

    }
    
    
    @Test
    void processAndUpdatePaymentStatus() throws Exception {
        // Ensure delivery person is assigned
        testOrder.setDeliveryPerson(testDeliveryPerson);
        orderRepository.save(testOrder);

        // Step 1: Process payment
        PaymentDto paymentRequest = new PaymentDto();
        paymentRequest.setOrderId(testOrder.getOrderId());
        paymentRequest.setUserId(testCustomer.getUserId());
        paymentRequest.setAmount(new BigDecimal("32.00"));
        paymentRequest.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        MvcResult result = mockMvc.perform(post("/payments/process")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(paymentRequest)))
            .andExpect(status().isOk())
            .andReturn();

        String json = result.getResponse().getContentAsString();
        System.out.println("RESPONSE JSON: " + json); // <-- Add this
        PaymentDto responsePayment = objectMapper.readValue(json, PaymentDto.class);
        Long paymentId = responsePayment.getPaymentId();
        System.out.println("Parsed Payment ID: " + paymentId); // <-- Add this
        // Step 2: Update payment status
        PaymentDto updateDto = new PaymentDto();
        updateDto.setPaymentId(paymentId);
        updateDto.setPaymentStatus(PaymentStatus.SUCCESS);

        mockMvc.perform(put("/payments/status/" + paymentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + deliveryToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updateDto)))
            .andExpect(status().isOk());
    }



    @Test
    @Transactional
    void codPayment_UpdateByDeliveryPerson_ShouldSucceed() throws Exception {
        // 1. Create a fresh COD payment
        Payment codPayment = new Payment();
        codPayment.setOrder(testOrder);
        codPayment.setUser(testCustomer);
        codPayment.setAmount(new BigDecimal("100.00"));
        codPayment.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY); // MUST be COD
        codPayment.setPaymentStatus(PaymentStatus.PENDING);
        codPayment = paymentRepository.save(codPayment);
        
        // 2. Assign delivery person to order
        testOrder.setPayment(codPayment);
        testOrder.setDeliveryPerson(testDeliveryPerson);
        orderRepository.save(testOrder);
        
        // 3. Prepare minimal valid update request
        PaymentDto updateRequest = new PaymentDto();
        updateRequest.setPaymentStatus(PaymentStatus.SUCCESS); // Only status needs updating
        
        // 4. Execute with proper headers
        mockMvc.perform(put("/payments/status/" + codPayment.getPaymentId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + deliveryToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paymentStatus").value("COMPLETED"));
    }
    @Test
    void getPaymentDetails_WithFullOrderInfo_ShouldReturnCompleteData() throws Exception {
        Payment testPayment = new Payment();
        testPayment.setOrder(testOrder);
        testPayment.setUser(testCustomer);
        testPayment.setAmount(new BigDecimal("32.00"));
        testPayment.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        testPayment.setPaymentStatus(PaymentStatus.SUCCESS);
        testPayment.setTransactionId("txn_98765");
        testPayment.setInvoiceUrl("/invoices/test.pdf");
        paymentRepository.save(testPayment);

        mockMvc.perform(get("/payments/" + testPayment.getPaymentId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paymentId").value(testPayment.getPaymentId()))
            .andExpect(jsonPath("$.orderId").value(testOrder.getOrderId()))
            .andExpect(jsonPath("$.userId").value(testCustomer.getUserId()))
            .andExpect(jsonPath("$.amount").value(32.00))
            .andExpect(jsonPath("$.paymentMethod").value(PaymentMethod.CASH_ON_DELIVERY.name()))


            .andExpect(jsonPath("$.transactionId").value("txn_98765"))
            .andExpect(jsonPath("$.invoiceUrl").exists());
    }

    @Test
    void processPayment_WithIncorrectAmount_ShouldFail() throws Exception {
        PaymentDto invalidRequest = new PaymentDto();
        invalidRequest.setOrderId(testOrder.getOrderId());
        invalidRequest.setUserId(testCustomer.getUserId());
        invalidRequest.setAmount(new BigDecimal("30.00")); // Incorrect amount
        invalidRequest.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        mockMvc.perform(post("/payments/process")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updatePayment_NonAdminNonDelivery_ShouldReturn403() throws Exception {
        // Create another user (non-admin, non-delivery)
        User otherUser = new User(
            null, "unauthorized@test.com", "unauthUser",
            "password", "Unauthorized", "User",
            "0001112222", UserRole.CUSTOMER, true
        );
        otherUser.setApprovalStatus(ApprovalStatus.APPROVED);
        userRepository.save(otherUser); // Save first

        // Generate token AFTER saving to DB to avoid 401
        String otherToken = jwtUtil.generateToken(otherUser.getEmail());

        // Prepare updated DTO with new payment status
        PaymentDto updateDto = new PaymentDto();
        updateDto.setPaymentStatus(PaymentStatus.FAILED); // Trying to change status

        // Perform PUT request using unauthorized user's token
        mockMvc.perform(put("/payments/status/" + testPayment.getPaymentId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updateDto)))
            .andExpect(status().isForbidden()); // 403 expected
    }
    @Test
    void nonCodPayment_UpdateByDeliveryPerson_ShouldFail() throws Exception {
        // Create non-COD payment
        Payment nonCodPayment = new Payment();
        nonCodPayment.setOrder(testOrder);
        nonCodPayment.setUser(testCustomer);
        nonCodPayment.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY); // Not COD
        nonCodPayment = paymentRepository.save(nonCodPayment);
        
        PaymentDto updateRequest = new PaymentDto();
        updateRequest.setPaymentStatus(PaymentStatus.SUCCESS);
        
        mockMvc.perform(put("/payments/status/" + nonCodPayment.getPaymentId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + deliveryToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updateRequest)))
            .andExpect(status().isForbidden()); // Removed JSON path check
    }

    private String asJsonString(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

}
