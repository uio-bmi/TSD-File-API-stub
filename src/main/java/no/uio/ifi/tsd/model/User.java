package no.uio.ifi.tsd.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class User {
	@JsonProperty("user_name")
	private String userName;
	private String otp;
	private String password;
}
