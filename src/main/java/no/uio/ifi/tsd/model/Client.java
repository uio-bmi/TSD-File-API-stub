package no.uio.ifi.tsd.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Entity
@Table(name = "CLIENTS")
public class Client {
	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "ID", updatable = false, nullable = false)
	@ColumnDefault("random_uuid()")
	@JsonProperty("client_id")
	private String clientId;

	@SerializedName("EMAIL")
	private String email;

	@SerializedName("client_name")
	@JsonProperty("client_name")
	private String clientName;

	@SerializedName("confirmation_token")
	@JsonProperty("confirmation_token")
	private String confirmationToken;
	
	@SerializedName("user_name")
	@JsonProperty("user_name")
	private String userName;

	@SerializedName("password")
	private String password;
}
