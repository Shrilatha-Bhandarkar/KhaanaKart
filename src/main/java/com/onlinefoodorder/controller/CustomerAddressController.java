package com.onlinefoodorder.controller;

import com.onlinefoodorder.dto.CustomerAddressDto;
import com.onlinefoodorder.service.CustomerAddressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * Controller for handling customer address-related operations, such as adding,
 * updating, and deleting addresses.
 */
@RestController
@RequestMapping("/customer/addresses")
public class CustomerAddressController {

	private static final Logger logger = LoggerFactory.getLogger(CustomerAddressController.class);
	@Autowired
	private CustomerAddressService customerAddressService;

	/**
	 * Retrieves all addresses associated with the authenticated user.
	 *
	 * @param principal the authenticated user.
	 * @return list of user addresses.
	 */
	@GetMapping
	public ResponseEntity<List<CustomerAddressDto>> getUserAddresses(Principal principal) {
		logger.info("Fetching addresses for user: {}", principal.getName());
		return ResponseEntity.ok(customerAddressService.getAddressesByUser(principal.getName()));
	}

	/**
	 * Adds a new address for the authenticated user.
	 *
	 * @param principal  the authenticated user.
	 * @param addressDto the address details to add.
	 * @return the newly added address.
	 */
	@PostMapping
	public ResponseEntity<CustomerAddressDto> addAddress(Principal principal,
			@RequestBody CustomerAddressDto addressDto) {
		logger.info("Adding address for user: {}", principal.getName());
		return ResponseEntity.ok(customerAddressService.addAddress(principal.getName(), addressDto));
	}

	/**
	 * Updates an existing address.
	 *
	 * @param principal  the authenticated user.
	 * @param addressId  the ID of the address to update.
	 * @param addressDto the updated address details.
	 * @return the updated address.
	 */
	@PutMapping("/{addressId}")
	public ResponseEntity<CustomerAddressDto> updateAddress(Principal principal, @PathVariable Long addressId,
			@RequestBody CustomerAddressDto addressDto) {
		logger.info("Updating address {} for user {}", addressId, principal.getName());
		return ResponseEntity.ok(customerAddressService.updateAddress(principal.getName(), addressId, addressDto));
	}

	/**
	 * Deletes an address belonging to the authenticated user.
	 *
	 * @param principal the authenticated user.
	 * @param addressId the ID of the address to delete.
	 * @return HTTP 204 No Content if successful.
	 */
	@DeleteMapping("/{addressId}")
	public ResponseEntity<Void> deleteAddress(Principal principal, @PathVariable Long addressId) {
		logger.info("Deleting address {} for user {}", addressId, principal.getName());
		customerAddressService.deleteAddress(addressId, principal.getName());
		return ResponseEntity.noContent().build();
	}
}
