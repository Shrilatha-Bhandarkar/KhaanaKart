package com.onlinefoodorder.dto;

/**
 * DTO for authentication response containing tokens
 */

public class AuthResponse {
    private String token;

    //Getter
	public String getToken() {
		return token;
	}

	//Setter
	public void setToken(String token) {
		this.token = token;
	}

	public AuthResponse() {
		super();
	}

	public AuthResponse(String token) {
		super();
		this.token = token;
	}
}
