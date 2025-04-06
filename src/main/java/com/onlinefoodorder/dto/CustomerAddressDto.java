package com.onlinefoodorder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
/**
 * DTO for managing Customer details.
 */
public class CustomerAddressDto {
	private Long addressId;
	
	@NotBlank(message = "Address line 1 is required")
	private String addressLine1;

	private String addressLine2;
	@NotBlank(message = "City is required")
	
	private String city;
	
	private String state;
	
	@Pattern(regexp = "^[0-9]{5}(?:-[0-9]{4})?$", message = "Invalid postal code")
	private String postalCode;
	
	private String country = "India";

	// Constructors
	public CustomerAddressDto() {
	}

	public Long getAddressId() {
		return addressId;
	}

	public void setAddressId(Long addressId) {
		this.addressId = addressId;
	}

	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public CustomerAddressDto(Long addressId, String addressLine1, String addressLine2, String city, String state,
			String postalCode) {
		this.addressId = addressId;
		this.addressLine1 = addressLine1;
		this.addressLine2 = addressLine2;
		this.city = city;
		this.state = state;
		this.postalCode = postalCode;
	}
    public CustomerAddressDto(Long addressId, String addressLine1, String addressLine2, String city, String state,
            String postalCode, String country) {
        this.addressId = addressId;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
    }
}
