# 🍽️ KhaanaKart - Online Food Ordering System

KhaanaKart is a full-stack **Spring Boot**-based **Online Food Ordering System** that provides a seamless interface for users to browse restaurants, add items to their cart, place orders, make payments, and submit reviews. It supports multiple roles like **Admin**, **Restaurant Owner**, **Customer**, and **Delivery Person** with **JWT-based authentication** and **RBAC (Role-Based Access Control)**.

---

## 🔧 Tech Stack

- **Backend:** Spring Boot, Spring Security, JWT, JPA/Hibernate  
- **Database:** MySQL  
- **Build Tool:** Maven  
- **Security:** Spring Security + JWT  
- **Monitoring & Logging:** SLF4J, Logback, Spring Boot Actuator  
- **Testing:** JUnit  
- **Deployment:** WAR for Tomcat / Cloud Deployment Ready  

---

## 📁 Package Structure

```plaintext
com.onlinefoodorder
├── OnlineFoodOrderingSystemApplication.java   # Main application entry point

├── config/                                    # Security and JWT configuration
│   ├── JwtAuthenticationFilter.java
│   ├── JwtUtil.java
│   └── SecurityConfig.java

├── controller/                                # REST API Controllers
│   ├── AdminController.java
│   ├── AuthController.java
│   ├── CartController.java
│   ├── CouponController.java
│   ├── CustomerAddressController.java
│   ├── DeliveryAddressController.java
│   ├── MenuCategoryController.java
│   ├── MenuItemController.java
│   ├── OrderController.java
│   ├── PaymentController.java
│   ├── RestaurantController.java
│   ├── ReviewController.java
│   └── UserController.java

├── dto/                                       # Data Transfer Objects
│   ├── AuthRequest.java
│   ├── AuthResponse.java
│   ├── CartDto.java
│   ├── CouponDto.java
│   ├── CustomerAddressDto.java
│   ├── DeliveryAddressDto.java
│   ├── MenuCategoryDto.java
│   ├── MenuItemDto.java
│   ├── OrderDto.java
│   ├── OrderItemDto.java
│   ├── PaymentDto.java
│   ├── RestaurantDto.java
│   ├── ReviewDto.java
│   └── UserRegistrationDto.java

├── entity/                                    # JPA Entities
│   ├── Cart.java
│   ├── CartItem.java
│   ├── Coupon.java
│   ├── CustomerAddress.java
│   ├── DeliveryAddress.java
│   ├── MenuCategory.java
│   ├── MenuItem.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── Payment.java
│   ├── Restaurant.java
│   ├── Review.java
│   └── User.java

├── exception/                                 # Custom Exceptions
│   ├── DeliveryException.java
│   ├── GlobalExceptionHandler.java
│   ├── OrderNotFoundException.java
│   ├── PaymentFailedException.java
│   ├── ResourceNotFoundException.java
│   ├── RestaurantNotFoundException.java
│   ├── ReviewNotFoundException.java
│   ├── UnauthorizedAccessException.java
│   └── UserNotFoundException.java

├── repository/                                # Spring Data JPA Repositories
│   ├── CartItemRepository.java
│   ├── CartRepository.java
│   ├── CouponRepository.java
│   ├── CustomerAddressRepository.java
│   ├── MenuCategoryRepository.java
│   ├── MenuItemRepository.java
│   ├── OrderItemRepository.java
│   ├── OrderRepository.java
│   ├── PaymentRepository.java
│   ├── RestaurantRepository.java
│   ├── ReviewRepository.java
│   └── UserRepository.java

├── service/                                   # Business Logic Services
│   ├── AuthService.java
│   ├── CartService.java
│   ├── CostomerAddressServicce.java
│   ├── CouponService.java
│   ├── DeliveryService.java
│   ├── MenuCategoryService.java
│   ├── MenuItemService.java
│   ├── OrderItemService.java
│   ├── OrderService.java
│   ├── PaymentService.java
│   ├── RestaurantService.java
│   ├── ReviewService.java
│   └── UserService.java

├── util/                                      # Utility classes
│   ├── PdfGenerator.java
│   └── Status.java

🔐 Security Features
JWT-based authentication
Role-based access control (ADMIN, USER, DELIVERY, RESTAURANT_OWNER)
Unauthorized access and custom exception handling

✅ Features
👥 User Authentication & Authorization
User registration and login
Role-based access (RBAC)
Admin approval for restaurant/delivery/admin registrations

🍽️ Restaurant & Menu Management
Add/edit/delete restaurant and menu items (only for approved restaurant owners)
Categorize menu items

🛒 Cart & Checkout
Add/remove items from cart
Proceed to order and payment

💳 Payments
Track transactions
Integrate with future payment gateways (mock implemented)

🚚 Orders & Delivery
Place orders
Manage delivery addresses and tracking

⭐ Reviews
Customers can review restaurants and menu items

💸 Coupons & Discounts
Apply promotional codes and discounts

📦 Reports
PDF invoice generation
Order summaries


🚀 How to Run
1. Clone the repo
git clone https://github.com/Shrilatha-Bhandarkar/khaanakart.git
cd khaanakart

2. Setup MySQL Database
CREATE DATABASE khaanakart;

3. Update application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/khaanakart
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password

4. Build & Run the Application
./mvnw spring-boot:run

📬 API Endpoints
/api/auth/** – Login/Register
/api/user/** – User profile & management
/api/restaurants/** – Restaurant data
/api/menu/** – Menu items and categories
/api/cart/** – Cart operations
/api/orders/** – Place and track orders
/api/payments/** – Payment operations
/api/reviews/** – Submit and view reviews
Use Postman or Swagger (if enabled) to test these endpoints.

✍️ Contribution Guidelines
Fork the repo and create your branch.
Follow existing code style and naming conventions.
Submit a pull request with detailed explanation.

