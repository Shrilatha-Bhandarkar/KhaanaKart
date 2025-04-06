package com.onlinefoodorder.repository;

import com.onlinefoodorder.dto.DashboardStatsDto;
import com.onlinefoodorder.dto.MenuItemDto;
import com.onlinefoodorder.entity.OrderItem;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
	/**
	 * Retrieve a list of top-selling menu items across all orders.
	 * 
	 * @return list of ItemStat DTOs containing item names and total quantities sold, sorted in descending order.
	 */

    @Query("SELECT NEW com.onlinefoodorder.dto.DashboardStatsDto$ItemStat(mi.name, SUM(oi.quantity)) " +
            "FROM OrderItem oi JOIN oi.menuItem mi GROUP BY mi.name ORDER BY SUM(oi.quantity) DESC")
     List<DashboardStatsDto.ItemStat> findTopSellingItems();
   
    /**
     * Retrieve sales statistics (item name and quantity sold) for a specific restaurant owner.
     * 
     * @param email the email of the restaurant owner.
     * @return list of object arrays where each entry contains item name and total quantity sold.
     */

    @Query("SELECT mi.name, SUM(oi.quantity) " +
    	       "FROM OrderItem oi " +
    	       "JOIN oi.menuItem mi " +
    	       "JOIN mi.restaurant r " +
    	       "JOIN r.owner u " +
    	       "WHERE u.email = :email " +
    	       "GROUP BY mi.name")
    	List<Object[]> findSalesStatsByOwner(@Param("email") String email);

}
