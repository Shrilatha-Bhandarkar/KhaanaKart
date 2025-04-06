package com.onlinefoodorder.repository;

import com.onlinefoodorder.dto.DashboardStatsDto;
import com.onlinefoodorder.entity.Order;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.util.Status.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

	/**
	 * Retrieves all orders placed by a specific user.
	 * 
	 * @param user The user entity.
	 * @return List of orders.
	 */
	List<Order> findByUser(User user);

	/**
	 * Retrieves all orders assigned to a specific delivery person.
	 * 
	 * @param deliveryPerson The delivery person entity.
	 * @return List of orders assigned to the delivery person.
	 */
	List<Order> findByDeliveryPerson(User deliveryPerson);

	/**
	 * Retrieves orders assigned to a delivery person with a specific status.
	 * 
	 * @param deliveryPersonId The delivery person ID.
	 * @param status           The order status.
	 * @return List of matching orders.
	 */
	List<Order> findByDeliveryPerson_UserIdAndStatus(Long deliveryPersonId, OrderStatus status);

	/**
	 * Counts the number of times a user has used a specific coupon.
	 * 
	 * @param userId   The user ID.
	 * @param couponId The coupon ID.
	 * @return Number of times the coupon was used by the user.
	 */
	long countByUserUserIdAndCouponId(Long userId, Long couponId);

	long countByCreatedAtAfter(LocalDateTime date);

	/**
	 * Find all orders created after a specific date and time.
	 * 
	 * @param date the LocalDateTime to compare against.
	 * @return list of orders created after the given date.
	 */

	List<Order> findByCreatedAtAfter(LocalDateTime date);

	/**
	 * Retrieve the most active users based on the number of orders placed.
	 * 
	 * @return list of UserStat DTOs containing username and order count, sorted by
	 *         order count in descending order.
	 */

	@Query("SELECT NEW com.onlinefoodorder.dto.DashboardStatsDto$UserStat(u.username, COUNT(o)) "
			+ "FROM Order o JOIN o.user u GROUP BY u.username ORDER BY COUNT(o) DESC")
	List<DashboardStatsDto.UserStat> findMostActiveUsers();

	/**
	 * Retrieve top restaurants ranked by total number of orders.
	 * 
	 * @return list of RestaurantStat DTOs with restaurant name and order count.
	 */

	@Query("SELECT NEW com.onlinefoodorder.dto.DashboardStatsDto$RestaurantStat(r.name, COUNT(o)) "
			+ "FROM Order o JOIN o.restaurant r " + "GROUP BY r.name " + "ORDER BY COUNT(o) DESC")
	List<DashboardStatsDto.RestaurantStat> findTopRestaurantsByOrderCount();

	/**
	 * Retrieve top restaurants ranked by total revenue generated.
	 * 
	 * @return list of RestaurantStat DTOs with restaurant name and total revenue.
	 */

	@Query("SELECT NEW com.onlinefoodorder.dto.DashboardStatsDto$RestaurantStat(r.name, SUM(o.totalAmount)) "
			+ "FROM Order o JOIN o.restaurant r " + "GROUP BY r.name " + "ORDER BY SUM(o.totalAmount) DESC")
	List<DashboardStatsDto.RestaurantStat> findTopRestaurantsByRevenue();

	/**
	 * Count the total number of orders delivered by a specific delivery person,
	 * filtered by order status.
	 * 
	 * @param email  the email of the delivery person.
	 * @param status the order status to filter by.
	 * @return list of object arrays containing delivery person's username and order
	 *         count.
	 */

	@Query("SELECT d.username, COUNT(o) FROM Order o " + "JOIN o.deliveryPerson d " + "WHERE d.email = :email "
			+ "AND o.status = :status " + // Expecting the OrderStatus enum here
			"GROUP BY d.username")
	List<Object[]> getTotalOrdersDeliveredByEmail(@Param("email") String email, @Param("status") OrderStatus status);

	/**
	 * Get daily delivered order counts for a specific delivery person.
	 * 
	 * @param email the email of the delivery person.
	 * @return list of object arrays with date and number of delivered orders on
	 *         that date.
	 */

	@Query("SELECT DATE(o.createdAt), COUNT(o) FROM Order o " + "WHERE o.deliveryPerson.email = :email "
			+ "AND o.status = com.onlinefoodorder.util.Status.OrderStatus.DELIVERED "
			+ "GROUP BY DATE(o.createdAt) ORDER BY DATE(o.createdAt)")
	List<Object[]> getDeliveredOrdersPerDay(@Param("email") String email);

	/**
	 * Get total delivered order count grouped by delivery person's email.
	 * 
	 * @return list of object arrays containing delivery person's email and total
	 *         delivered order count.
	 */

	@Query("SELECT d.email, COUNT(o) FROM Order o " + "JOIN o.deliveryPerson d "
			+ "WHERE o.status = com.onlinefoodorder.util.Status.OrderStatus.DELIVERED " + "GROUP BY d.email")
	List<Object[]> getAllDeliveryPersonsStats();

}
