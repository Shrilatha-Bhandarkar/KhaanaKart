package com.onlinefoodorder.service;

import com.onlinefoodorder.dto.CustomerAddressDto;
import com.onlinefoodorder.entity.CustomerAddress;
import com.onlinefoodorder.entity.User;
import com.onlinefoodorder.exception.ResourceNotFoundException;
import com.onlinefoodorder.exception.UnauthorizedAccessException;
import com.onlinefoodorder.repository.CustomerAddressRepository;
import com.onlinefoodorder.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for handling customer address-related operations.
 */
@Service
public class CustomerAddressService {

	private static final Logger logger = LoggerFactory.getLogger(CustomerAddressService.class);

	@Autowired
	private CustomerAddressRepository customerAddressRepository;

	@Autowired
	private UserRepository userRepository;

	/**
	 * Retrieves all addresses associated with the given user.
	 *
	 * @param userEmail the email of the user.
	 * @return list of addresses.
	 */
	public List<CustomerAddressDto> getAddressesByUser(String userEmail) {
		logger.info("Fetching addresses for user {}", userEmail);
		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		return customerAddressRepository.findByUser(user).stream()
				.map(address -> new CustomerAddressDto(address.getAddressId(), address.getAddressLine1(),
						address.getAddressLine2(), address.getCity(), address.getState(), address.getPostalCode()))
				.collect(Collectors.toList());
	}

	/**
	 * Adds a new address for a user.
	 *
	 * @param userEmail  the email of the user.
	 * @param addressDto the address details.
	 * @return the saved address.
	 */
	@Transactional
	public CustomerAddressDto addAddress(String userEmail, CustomerAddressDto addressDto) {
		logger.info("Adding new address for user {}", userEmail);
		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		CustomerAddress address = new CustomerAddress(user, addressDto.getAddressLine1(), addressDto.getAddressLine2(),
				addressDto.getCity(), addressDto.getState(), addressDto.getPostalCode(), addressDto.getCountry());

		CustomerAddress savedAddress = customerAddressRepository.save(address);
		logger.info("Address added successfully for user {}", userEmail);

		return new CustomerAddressDto(savedAddress.getAddressId(), savedAddress.getAddressLine1(),
				savedAddress.getAddressLine2(), savedAddress.getCity(), savedAddress.getState(),
				savedAddress.getPostalCode());
	}

	/**
	 * Updates an existing address.
	 *
	 * @param userEmail  the email of the user.
	 * @param addressId  the ID of the address to update.
	 * @param addressDto the new address details.
	 * @return the updated address.
	 */
	@Transactional
	public CustomerAddressDto updateAddress(String userEmail, Long addressId, CustomerAddressDto addressDto) {
		logger.info("Updating address {} for user {}", addressId, userEmail);
		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		CustomerAddress address = customerAddressRepository.findById(addressId)
				.orElseThrow(() -> new ResourceNotFoundException("Address not found"));

		if (!address.getUser().getUserId().equals(user.getUserId())) {
			logger.warn("Unauthorized update attempt by user {}", userEmail);
			throw new UnauthorizedAccessException("You are not authorized to update this address");
		}

		address.setAddressLine1(addressDto.getAddressLine1());
		address.setAddressLine2(addressDto.getAddressLine2());
		address.setCity(addressDto.getCity());
		address.setState(addressDto.getState());
		address.setPostalCode(addressDto.getPostalCode());

		CustomerAddress updatedAddress = customerAddressRepository.save(address);
		logger.info("Address {} updated successfully for user {}", addressId, userEmail);

		return new CustomerAddressDto(updatedAddress.getAddressId(), updatedAddress.getAddressLine1(),
				updatedAddress.getAddressLine2(), updatedAddress.getCity(), updatedAddress.getState(),
				updatedAddress.getPostalCode());
	}

	/**
	 * Deletes an address.
	 *
	 * @param addressId the ID of the address.
	 * @param userEmail the email of the user.
	 */
	@Transactional
	public void deleteAddress(Long addressId, String userEmail) {
		logger.info("Deleting address {} for user {}", addressId, userEmail);
		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		CustomerAddress address = customerAddressRepository.findById(addressId)
				.orElseThrow(() -> new ResourceNotFoundException("Address not found"));

		if (!address.getUser().getUserId().equals(user.getUserId())) {
			logger.warn("Unauthorized delete attempt by user {}", userEmail);
			throw new UnauthorizedAccessException("You are not authorized to delete this address");
		}

		customerAddressRepository.delete(address);
		logger.info("Address {} deleted successfully for user {}", addressId, userEmail);
	}
}
