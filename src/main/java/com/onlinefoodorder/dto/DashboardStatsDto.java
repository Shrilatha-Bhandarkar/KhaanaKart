package com.onlinefoodorder.dto;


import java.math.BigDecimal;
import java.util.List;


public class DashboardStatsDto {

    private long totalUsers;
    private long totalOrdersToday;
    private BigDecimal totalRevenueThisMonth;
    private long totalRestaurants;

    private List<ItemStat> topSellingItems;
    private List<UserStat> mostActiveUsers;
    private List<RestaurantStat> topRestaurantsByOrders;
    private List<RestaurantStat> topRestaurantsByRevenue;


   

	public long getTotalUsers() {
		return totalUsers;
	}


	public void setTotalUsers(long totalUsers) {
		this.totalUsers = totalUsers;
	}


	public long getTotalOrdersToday() {
		return totalOrdersToday;
	}


	public void setTotalOrdersToday(long totalOrdersToday) {
		this.totalOrdersToday = totalOrdersToday;
	}


	public BigDecimal getTotalRevenueThisMonth() {
		return totalRevenueThisMonth;
	}


	public void setTotalRevenueThisMonth(BigDecimal totalRevenueThisMonth) {
		this.totalRevenueThisMonth = totalRevenueThisMonth;
	}


	public long getTotalRestaurants() {
		return totalRestaurants;
	}


	public void setTotalRestaurants(long totalRestaurants) {
		this.totalRestaurants = totalRestaurants;
	}


	public List<ItemStat> getTopSellingItems() {
		return topSellingItems;
	}


	public void setTopSellingItems(List<ItemStat> topSellingItems) {
		this.topSellingItems = topSellingItems;
	}


	public List<UserStat> getMostActiveUsers() {
		return mostActiveUsers;
	}


	public void setMostActiveUsers(List<UserStat> mostActiveUsers) {
		this.mostActiveUsers = mostActiveUsers;
	}


	public List<RestaurantStat> getTopRestaurantsByOrders() {
		return topRestaurantsByOrders;
	}


	public void setTopRestaurantsByOrders(List<RestaurantStat> topRestaurantsByOrders) {
		this.topRestaurantsByOrders = topRestaurantsByOrders;
	}


	public List<RestaurantStat> getTopRestaurantsByRevenue() {
		return topRestaurantsByRevenue;
	}


	public void setTopRestaurantsByRevenue(List<RestaurantStat> topRestaurantsByRevenue) {
		this.topRestaurantsByRevenue = topRestaurantsByRevenue;
	}
	
	
	public static class ItemStat {
	    private String itemName;
	    private long quantitySold;

	    // ✅ Constructor required for JPQL projection
	    public ItemStat(String itemName, long quantitySold) {
	        this.itemName = itemName;
	        this.quantitySold = quantitySold;
	    }

	    // Optional: No-arg constructor for Jackson (used in JSON deserialization)
	    public ItemStat() {}

	    public String getItemName() {
	        return itemName;
	    }

	    public void setItemName(String itemName) {
	        this.itemName = itemName;
	    }

	    public long getQuantitySold() {
	        return quantitySold;
	    }

	    public void setQuantitySold(long quantitySold) {
	        this.quantitySold = quantitySold;
	    }
	}


	 public static class UserStat {
		    private String username;
		    private long orderCount;

		    // ✅ Required by JPQL
		    public UserStat(String username, long orderCount) {
		        this.username = username;
		        this.orderCount = orderCount;
		    }

		    public UserStat() {} // for Jackson or if manually set

		    public String getUsername() {
		        return username;
		    }

		    public void setUsername(String username) {
		        this.username = username;
		    }

		    public long getOrderCount() {
		        return orderCount;
		    }

		    public void setOrderCount(long orderCount) {
		        this.orderCount = orderCount;
		    }
		}

	 public static class RestaurantStat {
		    private String restaurantName;
		    private BigDecimal value;

		    // ✅ JPQL-compatible constructor
		    public RestaurantStat(String restaurantName, Number value) {
		        this.restaurantName = restaurantName;
		        this.value = new BigDecimal(value.toString());
		    }

		    public RestaurantStat() {}

		    public String getRestaurantName() {
		        return restaurantName;
		    }

		    public void setRestaurantName(String restaurantName) {
		        this.restaurantName = restaurantName;
		    }

		    public BigDecimal getValue() {
		        return value;
		    }

		    public void setValue(BigDecimal value) {
		        this.value = value;
		    }
		}

    
}
