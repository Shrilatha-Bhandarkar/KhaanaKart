package com.onlinefoodorder.repository;

import com.onlinefoodorder.entity.CustomerAddress;
import com.onlinefoodorder.entity.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {

    /**
     * Retrieves all addresses associated with the given user.
     * 
     * @param user The user entity.
     * @return List of customer addresses.
     */
    List<CustomerAddress> findByUser(User user);
}
