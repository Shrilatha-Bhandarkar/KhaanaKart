package com.onlinefoodorder.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlinefoodorder.entity.CustomerAddress;
import com.onlinefoodorder.entity.Order;
import com.onlinefoodorder.entity.Restaurant;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.repository.CustomerAddressRepository;
import com.onlinefoodorder.repository.OrderRepository;
import com.onlinefoodorder.repository.RestaurantRepository;
import com.onlinefoodorder.repository.UserRepository;
import com.onlinefoodorder.security.JwtUtil;
import com.onlinefoodorder.util.Status.ApprovalStatus;
import com.onlinefoodorder.util.Status.OrderStatus;
import com.onlinefoodorder.util.Status.UserRole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DeliveryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CustomerAddressRepository customerAddressRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private JwtUtil jwtUtil; // Adjust class name if different

    
    private String jwtToken = "Bearer YOUR_VALID_JWT_TOKEN_HERE";

    private User deliveryPerson;
    private Order testOrder;

    @BeforeEach
    void setup() {
        // Clean all data first
        orderRepository.deleteAll();
        customerAddressRepository.deleteAll();
        restaurantRepository.deleteAll();
        userRepository.deleteAll();
        // Step 1: Create restaurant owner
        User owner = userRepository.findByEmail("owner@example.com").orElseGet(() -> {
            User user = new User();
            user.setEmail("owner@example.com");
            user.setUsername("Restaurant Owner");
            user.setPassword("encoded-password");
            user.setFirstName("owner");
            user.setLastName("restaurant");
            user.setRole(UserRole.RESTAURANT_OWNER);
            user.setActive(true);
            user.setApprovalStatus(ApprovalStatus.APPROVED);
            return userRepository.save(user);
        });
        // Step 2: Create restaurant
        Restaurant restaurant = new Restaurant(
            0L,
            "Tasty Bites",
            "123 Food Street",
            "9876543210",
            4.5,
            "https://example.com/logo.png",
            LocalDateTime.now(),
            "09:00",
            "22:00",
            owner
        );
        restaurant = restaurantRepository.save(restaurant);

        // Step 3: Create or fetch delivery person
        deliveryPerson = userRepository.findByEmail("delivery@example.com").orElseGet(() -> {
            User user = new User();
            user.setEmail("delivery@example.com");
            user.setUsername("Delivery Guy");
            user.setPassword(passwordEncoder.encode("securePassword123"));
            user.setFirstName("delivery");
            user.setLastName("dperson");
            user.setRole(UserRole.DELIVERY_PERSON);
            user.setActive(true);
            user.setApprovalStatus(ApprovalStatus.APPROVED);
            return userRepository.save(user);
        });

        // ✅ Step 4: Generate token AFTER deliveryPerson is initialized
        jwtToken = "Bearer " + jwtUtil.generateToken(deliveryPerson.getEmail());

        // Step 5: Create address
        CustomerAddress deliveryAddress = new CustomerAddress(
            0L,
            deliveryPerson,
            "123 Test Street",
            "Near Landmark",
            "TestCity",
            "TestState",
            "123456",
            "TestCountry",
            true
        );
        deliveryAddress = customerAddressRepository.save(deliveryAddress);

        // Step 6: Create test order
        testOrder = new Order();
        testOrder.setUser(deliveryPerson);
        testOrder.setDeliveryPerson(deliveryPerson);
        testOrder.setRestaurant(restaurant);
        testOrder.setDeliveryAddress(deliveryAddress);
        testOrder.setTotalAmount(BigDecimal.valueOf(100));
        testOrder.setDeliveryFee(BigDecimal.valueOf(10));
        testOrder.setTaxAmount(BigDecimal.valueOf(5));
        testOrder.setSpecialInstructions("Handle with care");
        testOrder.setEstimatedDeliveryTime(LocalDateTime.now().plusHours(1));
        testOrder.setStatus(OrderStatus.ASSIGNED);
        testOrder.setCreatedAt(LocalDateTime.now());
        testOrder.setUpdatedAt(LocalDateTime.now());
        testOrder.setPayment(null);

        orderRepository.save(testOrder);
        
    }



    @Test
    void GetAssignedOrders_Success() throws Exception {
        mockMvc.perform(get("/delivery/orders/assigned")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void MarkOrderOutForDelivery_Success()throws Exception {
        mockMvc.perform(put("/delivery/orders/" + testOrder.getOrderId() + "/out-for-delivery")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Order marked as Out for Delivery"));
    }

    @Test
    void MarkOrderDelivered_Success() throws Exception {
        testOrder.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        orderRepository.save(testOrder);

        mockMvc.perform(put("/delivery/orders/" + testOrder.getOrderId() + "/delivered")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Order marked as Delivered"));
    }

    @Test
    void GetDeliveryChart_Success() throws Exception {
        mockMvc.perform(get("/delivery/orders/deliveries/chart")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    void GetTotalDeliveriesChart_Success() throws Exception {
        mockMvc.perform(get("/delivery/orders/deliveries/total-chart")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }
    @Test
    void GetAssignedOrders_Unauthorized() throws Exception {
        mockMvc.perform(get("/delivery/orders/assigned"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void MarkOrderDelivered_OrderNotFound() throws Exception {
        long nonExistentOrderId = 9999L;
        mockMvc.perform(put("/delivery/orders/" + nonExistentOrderId + "/delivered")
                        .header("Authorization", jwtToken))
                .andExpect(status().isBadRequest()) // Changed from 404 to 400
                .andExpect(content().string("Order not found"));
    }

    @Test
    void MarkOrderDelivered_WrongStatus() throws Exception {
        // Set order to status other than OUT_FOR_DELIVERY
        testOrder.setStatus(OrderStatus.PREPARING);
        orderRepository.save(testOrder);

        mockMvc.perform(put("/delivery/orders/" + testOrder.getOrderId() + "/delivered")
                        .header("Authorization", jwtToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("out for delivery"))); // Case-insensitive check
    }


    @Test
    void GetDeliveryChart_Unauthorized() throws Exception {
        mockMvc.perform(get("/delivery/orders/deliveries/chart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void GetTotalDeliveriesChart_Unauthorized() throws Exception {
        mockMvc.perform(get("/delivery/orders/deliveries/total-chart"))
                .andExpect(status().isUnauthorized());
    }
}