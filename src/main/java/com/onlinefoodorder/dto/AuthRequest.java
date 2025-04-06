package com.onlinefoodorder.dto;
/**
 * DTO for authentication requests containing user credentials.
 */
public class AuthRequest {
	private String email;
	private String password;

	//Getter and Setter
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
